package com.duyvv.citizen_card_app.data.repository

import com.duyvv.citizen_card_app.data.dto.ApduResult
import com.duyvv.citizen_card_app.data.local.entity.Citizen
import com.duyvv.citizen_card_app.domain.ApplicationState
import com.duyvv.citizen_card_app.domain.repository.JavaCardRepository
import com.duyvv.citizen_card_app.utils.RSAUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.smartcardio.CommandAPDU
import javax.smartcardio.TerminalFactory

class JavaCardRepositoryImpl : JavaCardRepository {

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun connectCard(): Boolean = withContext(Dispatchers.IO) {
        val appletAID =
            byteArrayOf(0x11.toByte(), 0x22.toByte(), 0x33.toByte(), 0x44.toByte(), 0x55.toByte(), 0x00.toByte())
        val factory = TerminalFactory.getDefault()
        val terminals = factory.terminals()
        println("Số lượng thiết bị thẻ có sẵn: ${terminals.list().size}")
        if (terminals.list().isEmpty()) {
            println("Không tìm thấy thiết bị thẻ.")
            return@withContext false
        }
        terminals.list().onEach {
            println("Thiết bị thẻ: ${it.name}")
        }

        val terminal = terminals.list()[0]
        println("Đang kết nối tới thiết bị thẻ: ${terminal.name}")
        println("Card present: " + terminal.isCardPresent)
        if (!terminal.isCardPresent) {
            println("Không có thẻ nào được chèn vào thiết bị.")
            return@withContext false
        }

        ApplicationState.card = terminal.connect("T=1")
        println("Kết nối thành công tới thẻ: " + ApplicationState.card)

        val channel = ApplicationState.card!!.basicChannel
        val selectCommand = CommandAPDU(0x00, 0xA4, 0x04, 0x00, appletAID)
        val response = channel.transmit(selectCommand)

        println("Select Response: ${Integer.toHexString(response.sw)}")

        if (response.sw == 0x9000) {
            println("Gửi lệnh select thành công!")
            ApplicationState.setCardInserted(true) // Cập nhật trạng thái Flow
            return@withContext true
        } else {
            println("Gửi lệnh select thất bại. SW: ${Integer.toHexString(response.sw)}")
            return@withContext false
        }
    }

    suspend fun sendApdu(cla: Int, ins: Int, p1: Int, p2: Int, data: ByteArray?): ApduResult =
        withContext(Dispatchers.IO) {
            if (ApplicationState.card == null) {
                ApduResult.Failed("Không tìm thấy card!")
            } else {
                try {
                    val channel = ApplicationState.card!!.basicChannel
                    val apduStream = ByteArrayOutputStream()
                    apduStream.write(cla)
                    apduStream.write(ins)
                    apduStream.write(p1)
                    apduStream.write(p2)
                    val dataLength = data?.size ?: 0

                    apduStream.write((dataLength shr 16) and 0xFF)
                    apduStream.write((dataLength shr 8) and 0xFF)
                    apduStream.write(dataLength and 0xFF)

                    if (data != null) {
                        apduStream.write(data)
                    }

                    val command = CommandAPDU(apduStream.toByteArray())
                    println("Sending APDU: $command")
                    val response = channel.transmit(command)
                    println("APDU Response SW: ${Integer.toHexString(response.sw)}")
                    if (response.data.isNotEmpty()) {
                        // Arrays.toString -> contentToString() trong Kotlin
                        println("APDU Response Data: ${response.data.contentToString()}")
                    }
                    if (response.sw == 0x9000) {
                        ApduResult.Success(response.data)
                    } else {
                        System.err.println("APDU failed with status word: ${Integer.toHexString(response.sw)}")
                        ApduResult.Failed(message = "Lỗi gửi apdu", response = response.data)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ApduResult.Failed(message = e.message ?: "Lỗi gửi apdu không xác định!!")
                }
            }
        }

    override suspend fun isCardActive(): Boolean = withContext(Dispatchers.IO) {
        println("isCardActive")
        when (val result = sendApdu(0x00, 0x02, 0x05, 0x08, null)) {
            is ApduResult.Success -> {
                println("APDU command executed successfully!")
                val hexResponse = bytesToHex(result.response)
                println("response: $hexResponse")
                try {
                    // Chuyển Hex String sang Int (cơ số 16 để an toàn nếu kết quả là 0A, 0B...)
                    // Lưu ý: Java code cũ dùng Integer.parseInt mặc định là cơ số 10, có thể lỗi nếu hex có chữ cái.
                    val remainingAttempt = hexResponse?.trim()?.toIntOrNull(16) ?: 0

                    println("Remaining attempt: $remainingAttempt")
                    remainingAttempt > 0

                } catch (e: Exception) {
                    println("Lỗi phân tích số lần thử: ${e.message}")
                    false
                }
            }

            is ApduResult.Failed -> {
                println("Failed to execute APDU command.")
                println("response: ${bytesToHex(result.response)}")
                false
            }
        }
    }

    override suspend fun getCardId(): String? = withContext(Dispatchers.IO) {
        println("getCardId")
        when (val result = sendApdu(0x00, 0x02, 0x05, 0x0A, null)) {
            is ApduResult.Success -> {
                println("APDU command executed successfully!")
                println("response: " + bytesToHex(result.response))

                if (result.response == null) {
                    return@withContext null
                }

                if (result.response.size != 12) {
                    println("Invalid Card Id length.")
                    return@withContext null
                }

                val cardId = hexToString(bytesToHex(result.response))
                println("Card Id: $cardId")
                cardId
            }

            is ApduResult.Failed -> {
                println("Failed to execute APDU command.")
                println("response: " + bytesToHex(result.response))
                null
            }
        }
    }

    override suspend fun challengeCard(citizenId: String, storedPublicKey: String?): Boolean =
        withContext(Dispatchers.IO) {
            val challenge = Random().nextInt(1000000).toString()
            println("[DEBUG] Challenge: $challenge")
            println("[DEBUG] Stored public key: $storedPublicKey")
            if (storedPublicKey == null) return@withContext false

            val publicKey = parseHexStringToByteArray(storedPublicKey)
            println("[DEBUG] Public key: " + bytesToHex(publicKey))

            when (val result = sendApdu(0x00, 0x01, 0x06, 0x00, stringToHexArray(challenge))) {
                is ApduResult.Success -> {
                    println("APDU command executed successfully!")
                    println("response: " + bytesToHex(result.response))
                    println("[DEBUG] Signature Sucess: " + bytesToHex(result.response))
                    verifySignature(publicKey, result.response, challenge)
                }

                is ApduResult.Failed -> {
                    println("Failed to execute APDU command.")
                    val error = bytesToHex(result.response)
                    println("[DEBUG] Signature Failed: $error")
                    false
                }
            }
        }

    override suspend fun verifyCard(pinCode: String): Pair<Boolean, Int> = withContext(Dispatchers.IO) {
        println("verifyCard")
        when (val result = sendApdu(0x00, 0x00, 0x00, 0x00, stringToHexArray(pinCode))) {
            is ApduResult.Success -> {
                println("APDU command executed successfully!")
                println("response: " + bytesToHex(result.response))
                true to 5
            }

            is ApduResult.Failed -> {
                println("Failed to execute APDU command.")
                println("response: " + bytesToHex(result.response))
                false to (bytesToHex(result.response)?.toInt() ?: 0)
            }
        }
    }

    override suspend fun getCardInfo(): Citizen? = withContext(Dispatchers.IO) {
        println("getCardInfo")
        when (val result = sendApdu(0x00, 0x02, 0x05, 0x07, null)) {
            is ApduResult.Success -> {
                println("APDU command executed successfully!")
                println("=====>Card Response Data L1: ${bytesToHex(result.response)}")
                println("=====>Card Response Data: ${hexToString(bytesToHex(result.response))}")
                val citizen = Citizen.fromCardInfo(hexToStringUnicode(bytesToHex(result.response)))
                citizen.avatar = getAvatar()
                citizen
            }

            is ApduResult.Failed -> {
                println("Get Card info fail.")
                null
            }
        }
    }

    override suspend fun setupPinCode(
        pinCode: String,
        citizen: Citizen,
        latestId: String?,
        onResult: (Boolean, Citizen?, String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val newId = generateId(latestId)
        println("newid: $newId, latestId: $latestId")
        val citizen = citizen.copy(citizenId = newId)
        println("setupPinCode=====>" + bytesToHex(stringToHexArray(citizen.toCardInfo() + "$" + pinCode)))
        val formattedDate = SimpleDateFormat("dd/MM/yyyy").format(Date())
        val data = stringToHexArray("${citizen.toCardInfo()}$${formattedDate}$${pinCode}")
        when (val result = sendApdu(0x00, 0x01, 0x05, 0x00, data)) {
            is ApduResult.Success -> {
                ApplicationState.setCardInserted(true)
                ApplicationState.setCardVerified(true)
                val publicKey = bytesToHex(result.response)
                println("setupPinCode=====>publicKey: $publicKey")
                citizen.avatar?.let {
                    sendAvatar(it)
                }
                onResult(true, citizen, publicKey)
            }

            is ApduResult.Failed -> {
                println("setupPinCode=====>failed")
                onResult(false, null, null)
            }
        }
    }

    override suspend fun resetPinCode(
        pinCode: String,
        citizen: Citizen,
        onResult: (Boolean, Citizen?, String?) -> Unit
    ) {
        println("resetPinCode=====>" + bytesToHex(stringToHexArray(citizen.toCardInfo() + "$" + pinCode)))
        val formattedDate = SimpleDateFormat("dd/MM/yyyy").format(Date())
        val data = stringToHexArray("${citizen.toCardInfo()}$${formattedDate}$${pinCode}")
        when (val result = sendApdu(0x00, 0x01, 0x05, 0x00, data)) {
            is ApduResult.Success -> {
                ApplicationState.setCardInserted(true)
                ApplicationState.setCardVerified(true)
                val publicKey = bytesToHex(result.response)
                println("resetPinCode=====>publicKey: $publicKey")
                citizen.avatar?.let {
                    sendAvatar(it)
                }
                onResult(true, citizen, publicKey)
            }

            is ApduResult.Failed -> {
                println("setupPinCode=====>failed")
                onResult(false, null, null)
            }
        }
    }

    override suspend fun sendAvatar(avatar: ByteArray?): Boolean {
        return when (sendApdu(0x00, 0x03, 0x05, 0x09, avatar)) {
            is ApduResult.Success -> {
                println("sendAvatar success")
                true
            }

            is ApduResult.Failed -> {
                println("sendAvatar failed")
                false
            }
        }
    }

    private fun generateId(latestId: String?): String {
        val prefix = SimpleDateFormat("ddMMyy").format(Date())
        val subFix = if (latestId == null) {
            "000001"
        } else if (latestId.length >= 6) {
            val last6Chars = latestId.substring(latestId.length - 6)
            val last6Int = last6Chars.toInt()
            String.format("%06d", last6Int + 1)
        } else {
            "000001"
        }
        println("generateId: $prefix")
        return "$prefix$subFix"
    }

    private suspend fun getAvatar(): ByteArray? = withContext(Dispatchers.IO) {
        when (val result = sendApdu(0x00, 0x02, 0x05, 0x09, null)) {
            is ApduResult.Success -> {
                println("Get avatar success: " + bytesToHex(result.response))
                result.response
            }

            is ApduResult.Failed -> {
                println("Get avatar fail: " + bytesToHex(result.response))
                null
            }
        }
    }

    private fun verifySignature(publicKey: ByteArray?, signature: ByteArray?, challenge: String): Boolean {
        val key = RSAUtils.generatePublicKeyFromBytes(publicKey) ?: return false
        return RSAUtils.accuracy(signature, key, challenge)
    }

    fun parseHexStringToByteArray(hexString: String): ByteArray {
        return hexString.trim().split(" ")
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun stringToHexArray(str: String): ByteArray {
        return str.toByteArray(Charsets.UTF_8)
    }

    fun bytesToHex(bytes: ByteArray?): String? {
        return bytes?.joinToString(" ") { "%02X".format(it) }
    }

    override fun disconnectCard() {
        ApplicationState.reset()
    }

    override suspend fun changePin(oldPin: String, newPin: String): Boolean = withContext(Dispatchers.IO) {
        when (val result = sendApdu(0x00, 0x03, 0x04, 0x00, stringToHexArray("$oldPin$$newPin"))) {
            is ApduResult.Success -> {
                println("change pin success! ${bytesToHex(result.response)}")
                true
            }

            is ApduResult.Failed -> {
                println("change pin failed! ${bytesToHex(result.response)}")
                false
            }
        }
    }

    override suspend fun updateCardInfo(citizen: Citizen): Boolean = withContext(Dispatchers.IO) {
        when (val result = sendApdu(0x00, 0x03, 0x05, 0x07, stringToHexArray(citizen.toCardInfo()))) {
            is ApduResult.Success -> {
                println("Update data to card success: ${bytesToHex(result.response)}")
                true
            }

            is ApduResult.Failed -> {
                println("Update data to card failed: ${bytesToHex(result.response)}")
                false
            }
        }
    }

    override suspend fun lockCard(): Boolean = withContext(Dispatchers.IO) {
        when (val result = sendApdu(0x00, 0x03, 0x0C, 0x00, null)) {
            is ApduResult.Success -> {
                println("lock card success: ${bytesToHex(result.response)}")
                true
            }

            is ApduResult.Failed -> {
                println("lock card failed: ${bytesToHex(result.response)}")
                false
            }
        }
    }

    override suspend fun unlockCard(): Boolean = withContext(Dispatchers.IO) {
        when (val result = sendApdu(0x00, 0x03, 0x0B, 0x00, null)) {
            is ApduResult.Success -> {
                println("unlock card success: ${bytesToHex(result.response)}")
                true
            }

            is ApduResult.Failed -> {
                println("unlock card failed: ${bytesToHex(result.response)}")
                false
            }
        }
    }

    fun hexToString(hexInput: String?): String {
        val hex = hexInput?.replace(" ", "") ?: return ""
        println("Hex to string: $hex")
        println("=====>hex length: ${hex.length}")
        require(hex.length % 2 == 0) { "Invalid hex string length" }
        return hex.chunked(2)
            .map { it.toInt(16).toChar() }
            .joinToString("")
    }

    fun hexToStringUnicode(hexInput: String?): String {
        val hex = hexInput?.replace(" ", "")
        if (hex.isNullOrEmpty()) {
            return ""
        }
        require(hex.length % 2 == 0) { "Invalid hex string length" }
        val byteArray = hex.chunked(2)              // Cắt thành từng cặp ký tự: ["E1", "BB", "AF"]
            .map { it.toInt(16).toByte() }          // Chuyển từng cặp thành Byte
            .toByteArray()                          // Gom lại thành mảng ByteArray
        return String(byteArray, Charsets.UTF_8)
    }

    override suspend fun setupMultiLicenses(dataStr: String, count: Int): Boolean = withContext(Dispatchers.IO) {
        val dataBytes = stringToHexArray(dataStr)
        // Gửi lệnh 0x20, P2 = Số lượng bằng
        // dataStr dạng: "ID1|A1|Date1#ID2|B2|Date2"
        when (val result = sendApdu(0x00, 0x20, 0x00, count, dataBytes)) {
            is ApduResult.Success -> true
            is ApduResult.Failed -> false
        }
    }

    override suspend fun penalizeLicenseByIndex(points: Int, index: Int): Pair<Int, Boolean>? = withContext(Dispatchers.IO) {
        // Log xem mình gửi cái gì đi
        println("Sending Penalize: Points=$points, Index=$index")

        // Gửi lệnh 0x22, P1 = Điểm, P2 = Index
        when (val result = sendApdu(0x00, 0x22, points, index, null)) {
            is ApduResult.Success -> {
                val resp = result.response ?: return@withContext null

                // --- LOG DEBUG QUAN TRỌNG ---
                // Hãy xem logcat hiện gì.
                // Nếu hiện "Response Hex: 00 01" nghĩa là 0 điểm, bị khóa.
                // Nếu hiện "Response Hex: 0C 00" nghĩa là 12 điểm, mở.
                val hexString = resp.joinToString(" ") { "%02X".format(it) }
                println("Card Response Hex: $hexString")
                // -----------------------------

                // Trả về: [Score] [Status]
                if (resp.size >= 2) {
                    // SỬA LẠI: Dùng toInt() and 0xFF để đảm bảo lấy đúng số dương
                    val score = resp[0].toInt() and 0xFF
                    val statusByte = resp[1].toInt() and 0xFF

                    // Status: 1 là Khóa (Revoked), 0 là Mở
                    val isRevoked = (statusByte == 1)

                    println("Parsed Result: Score=$score, isRevoked=$isRevoked")

                    Pair(score, isRevoked)
                } else {
                    println("Error: Response length too short (<2 bytes)")
                    null
                }
            }
            is ApduResult.Failed -> {
                println("Error: Send APDU Failed - ${result.message}")
                null
            }
        }
    }
    // 3. Lấy toàn bộ danh sách từ thẻ
    override suspend fun getAllLicensesFromCard(): List<Triple<String, Int, Boolean>>? = withContext(Dispatchers.IO) {
        // Gửi lệnh 0x21
        val result = sendApdu(0x00, 0x21, 0x00, 0x00, null)
        if (result !is ApduResult.Success) return@withContext null

        val resp = result.response ?: return@withContext null

        // Tìm vị trí byte ngăn cách 0xFF (duyệt ngược từ cuối để an toàn)
        var sepIndex = -1
        for (i in resp.indices.reversed()) {
            if (resp[i] == 0xFF.toByte()) {
                sepIndex = i
                break
            }
        }
        if (sepIndex == -1) return@withContext null

        // A. Parse phần Text (Info1#Info2...)
        // Copy từ 0 đến sepIndex
        val textBytes = resp.copyOfRange(0, sepIndex)
        val fullText = String(textBytes, Charsets.UTF_8)

        // Nếu chuỗi rỗng thì trả về list rỗng
        if (fullText.isEmpty()) return@withContext emptyList()

        val textParts = fullText.split("#") // Tách chuỗi dựa trên dấu #

        // B. Parse phần Metadata (Count | Score1 Status1 | Score2 Status2...)
        // Byte tại sepIndex là 0xFF
        // Byte tại sepIndex + 1 là Count
        if (sepIndex + 1 >= resp.size) return@withContext emptyList()

        val count = resp[sepIndex + 1].toInt()
        val resultList = mutableListOf<Triple<String, Int, Boolean>>()

        var ptr = sepIndex + 2 // Con trỏ bắt đầu đọc điểm
        for (i in 0 until count) {
            // Đảm bảo không đọc vượt quá mảng text hoặc mảng byte response
            if (i >= textParts.size || ptr + 1 >= resp.size) break

            val score = resp[ptr].toInt()
            val isRevoked = resp[ptr + 1].toInt() == 1

            resultList.add(Triple(textParts[i], score, isRevoked))

            ptr += 2 // Nhảy 2 byte (1 byte Score + 1 byte Status) cho lần lặp sau
        }

        return@withContext resultList
    }

    override suspend fun resetLicenseByIndex(index: Int): Boolean = withContext(Dispatchers.IO) {
        // Gửi lệnh 0x23, P2 = index
        when (val result = sendApdu(0x00, 0x23, 0x00, index, null)) {
            is ApduResult.Success -> true
            is ApduResult.Failed -> false
        }
    }
}