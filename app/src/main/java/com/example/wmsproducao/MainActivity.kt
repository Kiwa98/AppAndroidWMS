package com.example.wmsproducao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wmsproducao.ui.theme.WMSProducaoTheme
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.example.wmsproducao.api.RetrofitClient
import com.example.wmsproducao.api.EstoqueDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WMSProducaoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EstoqueApp()
                }
            }
        }
    }
}

@Composable
fun EstoqueApp() {
    var tela by remember { mutableStateOf("menu") }

    when (tela) {
        "menu" -> MenuScreen(onNavigate = { tela = it })
        "cadastro" -> CadastroScreen(onBack = { tela = "menu" })
        "consulta" -> ConsultaScreen(onBack = { tela = "menu" })
        "transferencia" -> TransferenciaScreen(onBack = { tela = "menu" })
        "ver_estoque" -> VerEstoqueScreen(onBack = { tela = "menu" })
        "ver_transferencias" -> VerTransferenciasScreen(onBack = { tela = "menu" })
        "adicionar" -> AdicionarEstoqueScreen(onBack = { tela = "menu" })
    }
}

@Composable
fun MenuScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Controle de Estoque", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(30.dp))
        Button(onClick = { onNavigate("cadastro") }) { Text("Cadastrar Item") }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onNavigate("adicionar") }) { Text("Adicionar ao Estoque") }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onNavigate("consulta") }) { Text("Consultar Estoque") }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onNavigate("transferencia") }) { Text("Transferência") }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onNavigate("ver_estoque") }) { Text("Ver Estoque Completo") }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { onNavigate("ver_transferencias") }) { Text("Ver Transferências") }
    }
}

@Composable
fun CadastroScreen(onBack: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var ean by remember { mutableStateOf("") }
    var totvs by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val path = File(context.getExternalFilesDir(null), "estoque.json")

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cadastrar Novo Item", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") })
        OutlinedTextField(value = ean, onValueChange = { ean = it }, label = { Text("EAN") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = totvs, onValueChange = { totvs = it }, label = { Text("Código TOTVS") })
        OutlinedTextField(value = quantidade, onValueChange = { quantidade = it }, label = { Text("Quantidade") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = local, onValueChange = { local = it }, label = { Text("Localização (ex: A1, B2)") })
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            if (nome.isNotEmpty() && ean.isNotEmpty() && totvs.isNotEmpty() && quantidade.isNotEmpty()) {
                val jsonArray = if (path.exists()) JSONArray(path.readText()) else JSONArray()
                var duplicado = false
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    if (item.getString("ean") == ean) {
                        mensagem = "❌ Já existe um produto com esse EAN!"
                        duplicado = true
                        break
                    }
                    if (item.getString("totvs") == totvs) {
                        mensagem = "❌ Já existe um produto com esse código TOTVS!"
                        duplicado = true
                        break
                    }
                }
                if (!duplicado) {
                    val obj = JSONObject()
                    obj.put("nome", nome)
                    obj.put("ean", ean)
                    obj.put("totvs", totvs)
                    obj.put("quantidade", quantidade.toIntOrNull() ?: 0)
                    obj.put("local", local)
                    // ------ ENVIAR PARA API -------
                    val dto = EstoqueDTO(
                        nome = nome,
                        ean = ean,
                        totvs = totvs,
                        quantidade = quantidade.toInt(),
                        localizacao = local
                    )

                    RetrofitClient.instance.salvarEstoque(dto)
                        .enqueue(object : Callback<EstoqueDTO> {
                            override fun onResponse(call: Call<EstoqueDTO>, response: Response<EstoqueDTO>) {
                                if (response.isSuccessful) {
                                    val criado = response.body()
                                    // pega id retornado pela API (pode ser null)
                                    val returnedId = criado?.id
                                    if (returnedId != null) {
                                        // guarda o id no objeto local antes de salvar
                                        obj.put("id", returnedId)
                                    }
                                    mensagem = "✅ Produto cadastrado na API!"

                                    // salva local (backup): mantém as chaves que você já usa (nome, ean, totvs, quantidade, local)
                                    jsonArray.put(obj)
                                    path.writeText(jsonArray.toString())
                                } else {
                                    mensagem = "⚠️ Erro ao cadastrar na API!"
                                }
                            }

                            override fun onFailure(call: Call<EstoqueDTO>, t: Throwable) {
                                mensagem = "❌ Falha na API: ${t.message}"
                            }
                        })


                    nome = ""; ean = ""; totvs = ""; quantidade = ""; local = ""
                }
            } else {
                mensagem = "⚠️ Preencha todos os campos!"
            }
        }) { Text("Salvar") }
        Spacer(Modifier.height(10.dp))
        Text(mensagem)
        Spacer(Modifier.height(10.dp))
        Button(onClick = onBack) { Text("Voltar") }
    }
}

@Composable

fun AdicionarEstoqueScreen(onBack: () -> Unit) {
    var codigo by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val path = File(context.getExternalFilesDir(null), "estoque.json")

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Adicionar ao Estoque", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("EAN ou Código TOTVS") })
        OutlinedTextField(value = quantidade, onValueChange = { quantidade = it }, label = { Text("Quantidade a adicionar") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(Modifier.height(10.dp))
        Button(onClick = {
            if (path.exists()) {
                val data = JSONArray(path.readText())
                var encontrado = false
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    if (item.getString("ean") == codigo || item.getString("totvs") == codigo) {
                        val atual = item.getInt("quantidade")
                        val add = quantidade.toIntOrNull() ?: 0
                        item.put("quantidade", atual + add)
                        // ------- ENVIAR UPDATE PARA API ----------
                        val dto = EstoqueDTO(
                            nome = item.getString("nome"),
                            ean = item.getString("ean"),
                            totvs = item.getString("totvs"),
                            quantidade = atual + add,
                            localizacao = item.getString("local")
                        )

                        val id = item.optLong("id", -1)
                        if (id != -1L) {
                            RetrofitClient.instance.atualizar(id, dto)
                                .enqueue(object : Callback<EstoqueDTO> {
                                    override fun onResponse(call: Call<EstoqueDTO>, response: Response<EstoqueDTO>) {
                                        // apenas ignore se deu certo
                                    }
                                    override fun onFailure(call: Call<EstoqueDTO>, t: Throwable) {}
                                })
                        }

                        encontrado = true
                        break
                    }
                }
                if (encontrado) {
                    path.writeText(data.toString())
                    mensagem = "✅ Quantidade adicionada com sucesso!"
                } else mensagem = "⚠️ Item não encontrado."
            } else mensagem = "⚠️ Arquivo de estoque não encontrado."
            codigo = ""; quantidade = ""
        }) { Text("Adicionar") }
        Spacer(Modifier.height(20.dp))
        Text(mensagem)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Voltar") }
    }
}

@Composable
fun ConsultaScreen(onBack: () -> Unit) {
    var codigo by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val path = File(context.getExternalFilesDir(null), "estoque.json")

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Consultar Estoque", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Digite o EAN ou Código TOTVS") })
        Spacer(Modifier.height(10.dp))
        Button(onClick = {
            if (path.exists()) {
                val data = JSONArray(path.readText())
                var encontrado = ""
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    if (item.getString("ean") == codigo || item.getString("totvs") == codigo) {
                        encontrado = "Nome: ${item.getString("nome")}\nQtd: ${item.getInt("quantidade")}\nLocal: ${item.getString("local")}"
                        break
                    }
                }
                resultado = if (encontrado.isNotEmpty()) encontrado else "⚠️ Item não encontrado."
            } else resultado = "⚠️ Nenhum arquivo de estoque encontrado."
        }) { Text("Buscar") }
        Spacer(Modifier.height(20.dp))
        Text(resultado)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Voltar") }
    }
}

@Composable
fun TransferenciaScreen(onBack: () -> Unit) {
    var codigo by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }
    var linha by remember { mutableStateOf("") }
    var entreguePara by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val path = File(context.getExternalFilesDir(null), "estoque.json")
    val logPath = File(context.getExternalFilesDir(null), "transferencias.json")

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Transferência de Itens", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("EAN ou Código TOTVS") })
        OutlinedTextField(value = quantidade, onValueChange = { quantidade = it }, label = { Text("Quantidade") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = linha, onValueChange = { linha = it }, label = { Text("Linha de Produção (1 a 6)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = entreguePara, onValueChange = { entreguePara = it }, label = { Text("Entregue para (nome do operador)") })
        Spacer(Modifier.height(10.dp))
        Button(onClick = {
            if (codigo.isNotEmpty() && quantidade.isNotEmpty() && linha.isNotEmpty() && entreguePara.isNotEmpty()) {
                if (path.exists()) {
                    val data = JSONArray(path.readText())
                    var encontrado = false
                    for (i in 0 until data.length()) {
                        val item = data.getJSONObject(i)
                        if (item.getString("ean") == codigo || item.getString("totvs") == codigo) {
                            val qtd = item.getInt("quantidade")
                            val transf = quantidade.toIntOrNull() ?: 0
                            if (transf > 0 && transf <= qtd) {
                                item.put("quantidade", qtd - transf)
                                // ------- ENVIAR UPDATE PARA API ----------
                                val dto = EstoqueDTO(
                                    nome = item.getString("nome"),
                                    ean = item.getString("ean"),
                                    totvs = item.getString("totvs"),
                                    quantidade = qtd - transf,
                                    localizacao = item.getString("local")
                                )

                                val id = item.optLong("id", -1)
                                if (id != -1L) {
                                    RetrofitClient.instance.atualizar(id, dto)
                                        .enqueue(object : Callback<EstoqueDTO> {
                                            override fun onResponse(call: Call<EstoqueDTO>, response: Response<EstoqueDTO>) {}
                                            override fun onFailure(call: Call<EstoqueDTO>, t: Throwable) {}
                                        })
                                }

                                path.writeText(data.toString())
                                val logArray = if (logPath.exists()) JSONArray(logPath.readText()) else JSONArray()
                                val log = JSONObject()
                                val dataAtual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                log.put("codigo", codigo)
                                log.put("quantidade", transf)
                                log.put("linha", linha)
                                log.put("entregue_para", entreguePara)
                                log.put("data", dataAtual)
                                logArray.put(log)
                                logPath.writeText(logArray.toString())
                                mensagem = "✅ Transferência registrada com sucesso!"
                            } else {
                                mensagem = "⚠️ Quantidade inválida!"
                            }
                            encontrado = true
                            break
                        }
                    }
                    if (!encontrado) mensagem = "⚠️ Produto não encontrado."
                } else {
                    mensagem = "⚠️ Arquivo de estoque não encontrado."
                }
            } else {
                mensagem = "⚠️ Preencha todos os campos!"
            }
            codigo = ""; quantidade = ""; linha = ""; entreguePara = ""
        }) { Text("Registrar Transferência") }
        Spacer(Modifier.height(10.dp))
        Text(mensagem)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Voltar") }
    }
}

fun gerarExcelEstoque(context: android.content.Context): String {
    val path = File(context.getExternalFilesDir(null), "estoque.json")
    if (!path.exists()) return "⚠️ Nenhum arquivo de estoque encontrado."
    val jsonArray = JSONArray(path.readText())
    if (jsonArray.length() == 0) return "⚠️ Estoque vazio."

    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Estoque")
    val header = sheet.createRow(0)
    header.createCell(0).setCellValue("Nome")
    header.createCell(1).setCellValue("EAN")
    header.createCell(2).setCellValue("Código TOTVS")
    header.createCell(3).setCellValue("Quantidade")
    header.createCell(4).setCellValue("Local")
    for (i in 0 until jsonArray.length()) {
        val item = jsonArray.getJSONObject(i)
        val row = sheet.createRow(i + 1)
        row.createCell(0).setCellValue(item.getString("nome"))
        row.createCell(1).setCellValue(item.getString("ean"))
        row.createCell(2).setCellValue(item.getString("totvs"))
        row.createCell(3).setCellValue(item.getInt("quantidade").toDouble())
        row.createCell(4).setCellValue(item.getString("local"))
    }
    for (i in 0..4) sheet.autoSizeColumn(i)
    val file = File(context.getExternalFilesDir(null), "estoque_exportado.xlsx")
    FileOutputStream(file).use { workbook.write(it) }
    workbook.close()
    return "✅ Planilha criada com sucesso!\n${file.absolutePath}"
}

@Composable
fun VerEstoqueScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val path = File(context.getExternalFilesDir(null), "estoque.json")
    val lista = remember {
        if (path.exists()) {
            JSONArray(path.readText()).let { arr ->
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    "${obj.getString("nome")} | Qtd: ${obj.getInt("quantidade")} | Local: ${obj.getString("local")}"
                }
            }
        } else emptyList()
    }
    var mensagem by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.Start) {
        Text("Estoque Completo", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        lista.forEach { item -> Text("• $item") }
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            mensagem = gerarExcelEstoque(context)
        }) { Text("📄 Gerar Planilha Excel") }
        Spacer(Modifier.height(10.dp))
        Text(mensagem)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Voltar") }
    }
}

@Composable
fun VerTransferenciasScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val logPath = File(context.getExternalFilesDir(null), "transferencias.json")
    val lista = remember {
        if (logPath.exists()) {
            JSONArray(logPath.readText()).let { arr ->
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    "Código: ${obj.getString("codigo")} | Qtd: ${obj.getInt("quantidade")} | Linha: ${obj.optString("linha", "-")} | Entregue a: ${obj.optString("entregue_para", "-")} | Data: ${obj.getString("data")}"
                }
            }
        } else emptyList()
    }

    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.Start) {
        Text("Transferências Registradas", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        lista.forEach { item -> Text("• $item\n") }
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Voltar") }
    }
}
