package com.duyvv.citizen_card_app.data.service

import apdu4j.HexUtils
import apdu4j.TerminalManager
import com.duyvv.citizen_card_app.data.dto.ApduResult
import com.duyvv.citizen_card_app.domain.ApplicationState
import javax.smartcardio.CardException
import javax.smartcardio.CommandAPDU
import javax.smartcardio.ResponseAPDU

class JavaCardService {

    fun connectCard(onResult: (Boolean) -> Unit) {
        // Applet AID mặc định
        val appletAID = HexUtils.hex2bin("112233445500")

        try {
            // 1. Sử dụng TerminalManager của apdu4j để lấy danh sách đầu đọc an toàn hơn
            // null nghĩa là không lọc, lấy tất cả
            val terminals = TerminalManager.terminals(null)

            if (terminals.isEmpty()) {
                println("Không tìm thấy đầu đọc thẻ nào.")
                onResult(false)
                return
            }

            // 2. Logic thông minh: Tìm đầu đọc đầu tiên có thẻ đang cắm
            // Thay vì hardcode lấy phần tử thứ [1], ta lặp qua để tìm
            val activeTerminal = terminals.firstOrNull { it.isCardPresent }

            if (activeTerminal == null) {
                println("Tìm thấy ${terminals.size} đầu đọc nhưng không có thẻ nào được cắm.")
                onResult(false)
                return
            }

            println("Đang kết nối tới: ${activeTerminal.name}")

            // 3. Kết nối (dùng "*" để tự động chọn T=0 hoặc T=1)
            val card = activeTerminal.connect("*")

            // Lưu card vào Global State (giả định ApplicationState.card là kiểu javax.smartcardio.Card)
            ApplicationState.card = card
            ApplicationState.setCardInserted(true)

            println("Kết nối vật lý thành công. Protocol: ${card.protocol}")

            // 4. Gửi lệnh SELECT Applet
            val result = sendApdu(0x00, 0xA4, 0x04, 0x00, appletAID)

            when (result) {
                is ApduResult.Success -> {
                    println("SELECT Applet thành công!")
                    onResult(true)
                }
                is ApduResult.Failed -> {
                    println("SELECT Applet thất bại. Lỗi: ${result.message}")
                    // Nếu select lỗi thì coi như kết nối logic thất bại
                    disconnect()
                    onResult(false)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println("Lỗi nghiêm trọng khi kết nối: ${e.message}")
            onResult(false)
        }
    }

    fun sendApdu(cla: Int, ins: Int, p1: Int, p2: Int, data: ByteArray?): ApduResult {
        val card = ApplicationState.card
        if (card == null) return ApduResult.Failed("Chưa kết nối thẻ (Card object is null)")

        return try {
            val channel = card.basicChannel

            // 5. CẢI TIẾN LỚN: Dùng Constructor chuẩn, không cần tự tính bit shifting thủ công
            // CommandAPDU tự động xử lý độ dài Lc (length of data)
            val command = if (data != null && data.isNotEmpty()) {
                CommandAPDU(cla, ins, p1, p2, data)
            } else {
                CommandAPDU(cla, ins, p1, p2)
            }

            println(">> Gửi APDU: ${HexUtils.bin2hex(command.bytes)}")

            val response: ResponseAPDU = channel.transmit(command)

            val sw = response.sw
            val resData = response.data

            println("<< Nhận APDU: SW=${Integer.toHexString(sw)} | Data=${HexUtils.bin2hex(resData)}")

            if (sw == 0x9000) {
                ApduResult.Success(resData)
            } else {
                ApduResult.Failed(
                    message = "Lỗi thẻ trả về SW: ${Integer.toHexString(sw)}",
                    response = resData
                )
            }

        } catch (e: CardException) {
            e.printStackTrace()
            // Xử lý trường hợp thẻ bị rút đột ngột
            ApplicationState.setCardInserted(false)
            ApplicationState.card = null
            ApduResult.Failed("Mất kết nối thẻ: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            ApduResult.Failed("Lỗi không xác định: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            ApplicationState.card?.disconnect(false)
            ApplicationState.card = null
            println("Đã ngắt kết nối thẻ.")
        } catch (e: Exception) {
            println("Lỗi khi ngắt kết nối: ${e.message}")
        }
    }
}