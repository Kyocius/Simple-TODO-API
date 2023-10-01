import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

data class TodoList(val id: Int, var title: String)

var todoLists = mutableListOf<TodoList>()

fun main() {
    val serverSocket = ServerSocket(8000)
    println("Server running on port 8000")

    while (true) {
        val clientSocket = serverSocket.accept()
        println("Connected by ${clientSocket.inetAddress.hostAddress}:${clientSocket.port}")

        val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        val output = clientSocket.getOutputStream()

        val request = input.readLine()
        println("Received request: $request")

        val response = handleRequest(request)
        output.write(response.toByteArray())
        output.flush()

        clientSocket.close()
    }
}

fun handleRequest(request: String): String {
    val parts = request.split(" ")
    val method = parts[0]
    val path = parts[1]
    val body = parts[parts.size - 1]

    return when (method) {
        "GET" -> handleGetRequest(path)
        "POST" -> handlePostRequest(path, body)
        "DELETE" -> handleDeleteRequest(path)
        else -> "HTTP/1.1 400 Bad Request\r\n\r\n"
    }
}

fun handleGetRequest(path: String): String {
    val id = path.substringAfterLast("/api/list/")
    val todoList = todoLists.find { it.id == id.toInt() }
    val response = if (todoList != null) {
        "{\"id\":${todoList.id}, \"title\":\"${todoList.title}\"}"
    } else {
        ""
    }
    return "HTTP/1.1 200 OK\r\nContent-Length: ${response.length}\r\n\r\n$response"
}

fun handlePostRequest(path: String, body: String): String {
    return when {
        path.startsWith("/api/list/new") -> {
            val title = body.substringAfterLast("title\":\"").substringBefore("\"")
            val id = (1..100).random()
            val todoList = TodoList(id, title)
            todoLists.add(todoList)
            val response = "{\"id\":$id}"
            "HTTP/1.1 200 OK\r\nContent-Length: ${response.length}\r\n\r\n$response"
        }
        path.startsWith("/api/list/") -> {
            val id = path.substringAfterLast("/api/list/").toInt()
            val title = body.substringAfterLast("title\":\"").substringBefore("\"")
            val todoList = todoLists.find { it.id == id }
            if (todoList != null) {
                todoList.title = title
                "HTTP/1.1 200 OK\r\n\r\n"
            } else {
                "HTTP/1.1 404 Not Found\r\n\r\n"
            }
        }
        else -> "HTTP/1.1 400 Bad Request\r\n\r\n"
    }
}

fun handleDeleteRequest(path: String): String {
    val id = path.substringAfterLast("/api/list/").toInt()
    val todoList = todoLists.find { it.id == id }
    return if (todoList != null) {
        todoLists.remove(todoList)
        "HTTP/1.1 200 OK\r\n\r\n"
    } else {
        "HTTP/1.1 404 Not Found\r\n\r\n"
    }
}
