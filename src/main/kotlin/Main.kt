import java.io.*
import java.net.*

fun main() {
    val serverSocket = ServerSocket(8000)
    println("Server running on port 8000")

    while (true) {
        val clientSocket = serverSocket.accept()
        val clientThread = Thread(ClientHandler(clientSocket))
        clientThread.start()
    }
}

class ClientHandler(private val clientSocket: Socket) : Runnable {
    override fun run() {
        val request = readRequest()
        val response = processRequest(request)
        sendResponse(response)
        clientSocket.close()
    }

    private fun readRequest(): String {
        val inputStream = clientSocket.getInputStream()
        val reader = BufferedReader(InputStreamReader(inputStream))
        val request = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            if (line!!.isEmpty()) {
                break
            }
            request.append(line).append("\r\n")
        }

        return request.toString()
    }

    private fun processRequest(request: String): String {
        val lines = request.trim().split("\r\n")
        val method = lines[0].split(" ")[0]
        val path = lines[0].split(" ")[1]
        val body = lines.last()

        return when (method) {
            "GET" -> handleGetRequest(path)
            "POST" -> handlePostRequest(path, body)
            "DELETE" -> handleDeleteRequest(path)
            else -> "HTTP/1.1 400 Bad Request\r\n\r\n"
        }
    }

    private fun handleGetRequest(path: String): String {
        val id = path.substringAfterLast("/")
        val todoList = mutableList.find { it.id == id }

        return if (todoList != null) {
            val responseJson = """
                {"id": "${todoList.id}", "title": "${todoList.title}", "list": ${todoList.list}}
            """.trimIndent()
            "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n$responseJson"
        } else {
            "HTTP/1.1 404 Not Found\r\n\r\n"
        }
    }

    private fun handlePostRequest(path: String, body: String): String {
        // val id = body.substringAfter("\"id\": ").substringBefore(",").trim()
        val id = path.substringAfterLast("/")
        val todoList = mutableList.find { it.id == id }

        return if (todoList != null) {
            val updatedList = parseTodoListJson(body)
            todoList.list = updatedList.list
            "HTTP/1.1 200 OK\r\n\r\n"
        } else if (path == "/api/list/new") {
            val newTodoList = parseTodoListJson(body)
            mutableList.add(newTodoList)
            val responseJson = """
            {"id": "${newTodoList.id}"}
        """.trimIndent()
            "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n$responseJson"
        } else {
            "HTTP/1.1 404 Not Found\r\n\r\n"
        }
    }

    private fun handleDeleteRequest(path: String): String {
        val id = path.substringAfterLast("/")
        val todoList = mutableList.find { it.id == id }

        return if (todoList != null) {
            mutableList.remove(todoList)
            "HTTP/1.1 200 OK\r\n\r\n"
        } else {
            "HTTP/1.1 404 Not Found\r\n\r\n"
        }
    }

    private fun sendResponse(response: String) {
        val outputStream = clientSocket.getOutputStream()
        val writer = BufferedWriter(OutputStreamWriter(outputStream))
        writer.write(response)
        writer.flush()
    }

    private fun parseTodoListJson(json: String): TodoList {
        val id = json.substringAfter("\"id\": ").substringBefore(",").trim()
        val title = json.substringAfter("\"title\": ").substringBefore(",").trim()
        val list = json.substringAfter("\"list\": ").trim()

        return TodoList(id, title, list)
    }
}

data class TodoList(val id: String, val title: String, var list: String)

val mutableList = mutableListOf(
    TodoList("1", "Todo List 1", """[{"itemid": 1, "detail": "Item 1", "completed": false}]"""),
    TodoList("2", "Todo List 2", """[{"itemid": 1, "detail": "Item 1", "completed": true}, {"itemid": 2, "detail": "Item 2", "completed": false}]""")
)
