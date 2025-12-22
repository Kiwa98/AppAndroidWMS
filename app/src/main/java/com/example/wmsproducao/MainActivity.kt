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
import com.example.wmsproducao.api.*
import com.example.wmsproducao.ui.theme.WMSProducaoTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

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
        "menu" -> MenuScreen { tela = it }
        "cadastro" -> CadastroScreen { tela = "menu" }
        "consulta" -> ConsultaScreen { tela = "menu" }
        "transferencia" -> TransferenciaScreen { tela = "menu" }
        "ver_estoque" -> VerEstoqueScreen { tela = "menu" }
        "ver_transferencias" -> VerTransferenciasScreen { tela = "menu" }
        "adicionar" -> AdicionarEstoqueScreen { tela = "menu" }
    }
}

/* =========================
   COMPONENTES REUTILIZÁVEIS
   ========================= */

@Composable
fun MenuButton(text: String, route: String, onNavigate: (String) -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onNavigate(route) }
    ) {
        Text(text)
    }
}

@Composable
fun MensagemStatus(texto: String) {
    val cor = when {
        texto.startsWith("✅") -> MaterialTheme.colorScheme.primary
        texto.startsWith("⚠️") -> MaterialTheme.colorScheme.tertiary
        texto.startsWith("❌") -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text(text = texto, color = cor)
}

/* =========================
   MENU
   ========================= */

@Composable
fun MenuScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Controle de Estoque",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                MenuButton("Cadastrar Item", "cadastro", onNavigate)
                Spacer(Modifier.height(10.dp))

                MenuButton("Adicionar ao Estoque", "adicionar", onNavigate)
                Spacer(Modifier.height(10.dp))

                MenuButton("Consultar Estoque", "consulta", onNavigate)
                Spacer(Modifier.height(10.dp))

                MenuButton("Transferência", "transferencia", onNavigate)
                Spacer(Modifier.height(10.dp))

                MenuButton("Ver Estoque Completo", "ver_estoque", onNavigate)
                Spacer(Modifier.height(10.dp))

                MenuButton("Ver Transferências", "ver_transferencias", onNavigate)
            }
        }
    }
}

/* =========================
   CADASTRO
   ========================= */

@Composable
fun CadastroScreen(onBack: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var ean by remember { mutableStateOf("") }
    var totvs by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(24.dp)) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Cadastrar Produto", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    ean, { ean = it },
                    label = { Text("EAN") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(totvs, { totvs = it }, label = { Text("Código TOTVS") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    quantidade, { quantidade = it },
                    label = { Text("Quantidade") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(local, { local = it }, label = { Text("Localização") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (nome.isBlank() || ean.isBlank() || totvs.isBlank() || quantidade.isBlank()) {
                            mensagem = "⚠️ Preencha todos os campos"
                            return@Button
                        }

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
                                    mensagem = if (response.isSuccessful)
                                        "✅ Produto cadastrado com sucesso!"
                                    else
                                        "❌ EAN ou TOTVS duplicado"
                                }

                                override fun onFailure(call: Call<EstoqueDTO>, t: Throwable) {
                                    mensagem = "❌ Falha de conexão"
                                }
                            })
                    }
                ) { Text("Salvar") }

                Spacer(Modifier.height(12.dp))
                MensagemStatus(mensagem)

                Spacer(Modifier.height(12.dp))
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onBack) {
                    Text("Voltar")
                }
            }
        }
    }
}


@Composable
fun AdicionarEstoqueScreen(onBack: () -> Unit) {
    var codigo by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "Adicionar ao Estoque",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    codigo,
                    { codigo = it },
                    label = { Text("EAN ou TOTVS") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    quantidade,
                    { quantidade = it },
                    label = { Text("Quantidade") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        RetrofitClient.instance.buscar(codigo)
                            .enqueue(object : Callback<EstoqueDTO> {
                                override fun onResponse(
                                    call: Call<EstoqueDTO>,
                                    response: Response<EstoqueDTO>
                                ) {
                                    if (!response.isSuccessful || response.body() == null) {
                                        mensagem = "⚠️ Produto não encontrado"
                                        return
                                    }

                                    val item = response.body()!!
                                    val novaQtd = item.quantidade + (quantidade.toIntOrNull() ?: 0)

                                    RetrofitClient.instance
                                        .atualizar(item.id!!, item.copy(quantidade = novaQtd))
                                        .enqueue(object : Callback<EstoqueDTO> {
                                            override fun onResponse(
                                                call: Call<EstoqueDTO>,
                                                response: Response<EstoqueDTO>
                                            ) {
                                                mensagem = "✅ Estoque atualizado com sucesso"
                                            }

                                            override fun onFailure(call: Call<EstoqueDTO>, t: Throwable) {
                                                mensagem = "❌ Falha ao atualizar estoque"
                                            }
                                        })
                                }

                                override fun onFailure(call: Call<EstoqueDTO>, t: Throwable) {
                                    mensagem = "❌ Erro de conexão"
                                }
                            })
                    }
                ) {
                    Text("Adicionar")
                }

                Spacer(Modifier.height(12.dp))
                MensagemStatus(mensagem)

                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack
                ) {
                    Text("Voltar")
                }
            }
        }
    }
}


@Composable
fun ConsultaScreen(onBack: () -> Unit) {
    var codigo by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "Consultar Estoque",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    codigo,
                    { codigo = it },
                    label = { Text("EAN ou Código TOTVS") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        RetrofitClient.instance.buscar(codigo)
                            .enqueue(object : Callback<EstoqueDTO> {
                                override fun onResponse(
                                    call: Call<EstoqueDTO>,
                                    response: Response<EstoqueDTO>
                                ) {
                                    resultado =
                                        if (response.isSuccessful && response.body() != null) {
                                            val item = response.body()!!
                                            """
                                            Nome: ${item.nome ?: "-"}
                                            Quantidade: ${item.quantidade}
                                            Local: ${item.localizacao ?: "-"}
                                            """.trimIndent()
                                        } else {
                                            "⚠️ Produto não encontrado"
                                        }
                                }

                                override fun onFailure(call: Call<EstoqueDTO>, t: Throwable) {
                                    resultado = "❌ Erro ao consultar API"
                                }
                            })
                    }
                ) {
                    Text("Buscar")
                }

                Spacer(Modifier.height(16.dp))
                MensagemStatus(resultado)

                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack
                ) {
                    Text("Voltar")
                }
            }
        }
    }
}


@Composable
fun TransferenciaScreen(onBack: () -> Unit) {
    var codigo by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }
    var linha by remember { mutableStateOf("") }
    var entreguePara by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text(
                    "Transferência de Itens",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(codigo, { codigo = it }, label = { Text("EAN ou TOTVS") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    quantidade,
                    { quantidade = it },
                    label = { Text("Quantidade") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(linha, { linha = it }, label = { Text("Linha") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(entreguePara, { entreguePara = it }, label = { Text("Entregue para") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        RetrofitClient.instance.buscar(codigo)
                            .enqueue(object : Callback<EstoqueDTO> {
                                override fun onResponse(
                                    call: Call<EstoqueDTO>,
                                    response: Response<EstoqueDTO>
                                ) {
                                    val produto = response.body() ?: run {
                                        mensagem = "⚠️ Produto não encontrado"
                                        return
                                    }

                                    val transferencia = TransferenciaDTO(
                                        produtoId = produto.id!!,
                                        codigo = codigo,
                                        quantidade = quantidade.toIntOrNull() ?: 0,
                                        linha = linha,
                                        entreguePara = entreguePara,
                                        data = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                        ).format(Date())
                                    )

                                    RetrofitClient.instance.salvarTransferencia(transferencia)
                                        .enqueue(object : Callback<Void> {
                                            override fun onResponse(
                                                call: Call<Void>,
                                                response: Response<Void>
                                            ) {
                                                mensagem = "✅ Transferência registrada com sucesso"
                                            }

                                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                                mensagem = "❌ Erro ao registrar transferência"
                                            }
                                        })
                                }

                                override fun onFailure(call: Call<EstoqueDTO>, t: Throwable) {
                                    mensagem = "❌ Erro de conexão"
                                }
                            })
                    }
                ) {
                    Text("Registrar Transferência")
                }

                Spacer(Modifier.height(12.dp))
                MensagemStatus(mensagem)

                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack
                ) {
                    Text("Voltar")
                }
            }
        }
    }
}



@Composable
fun VerEstoqueScreen(onBack: () -> Unit) {
    val lista = remember { mutableStateListOf<EstoqueDTO>() }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.listar()
            .enqueue(object : Callback<List<EstoqueDTO>> {
                override fun onResponse(
                    call: Call<List<EstoqueDTO>>,
                    response: Response<List<EstoqueDTO>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        lista.clear()
                        lista.addAll(response.body()!!)
                    }
                }

                override fun onFailure(call: Call<List<EstoqueDTO>>, t: Throwable) {}
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            "Estoque Completo",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        if (lista.isEmpty()) {
            Text(
                "Nenhum produto encontrado.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        lista.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        text = item.nome ?: "Produto sem nome",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(6.dp))

                    Text("Quantidade: ${item.quantidade}")

                    Text(
                        "Local: ${item.localizacao ?: "-"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Voltar")
        }
    }
}


@Composable
fun VerTransferenciasScreen(onBack: () -> Unit) {
    val lista = remember { mutableStateListOf<TransferenciaDTO>() }

    LaunchedEffect(Unit) {
        RetrofitClient.instance.listarTransferencias()
            .enqueue(object : Callback<List<TransferenciaDTO>> {
                override fun onResponse(
                    call: Call<List<TransferenciaDTO>>,
                    response: Response<List<TransferenciaDTO>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        lista.clear()
                        lista.addAll(response.body()!!)
                    }
                }

                override fun onFailure(call: Call<List<TransferenciaDTO>>, t: Throwable) {}
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            "Transferências",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

        if (lista.isEmpty()) {
            Text(
                "Nenhuma transferência registrada.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        lista.forEach { t ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        text = t.codigo ?: "-",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(6.dp))

                    Text("Quantidade: ${t.quantidade}")
                    Text("Linha: ${t.linha ?: "-"}")

                    Text(
                        "Entregue para: ${t.entreguePara ?: "-"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack
        ) {
            Text("Voltar")
        }
    }
}


