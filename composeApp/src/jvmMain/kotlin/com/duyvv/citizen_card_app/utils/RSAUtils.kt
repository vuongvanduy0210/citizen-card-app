package com.duyvv.citizen_card_app.utils

import java.math.BigInteger
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec

object RSAUtils {

    fun generatePublicKeyFromBytes(data: ByteArray?): PublicKey? {
        if (data == null) return null
        return try {
            println("Try to generate public key")
            println("Data length: " + data.size)
            println(data.joinToString(" ") { "%02X".format(it) })
            val exponentLength = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
            val exponentBytes = data.copyOfRange(2, 2 + exponentLength)
            val modulusLengthIndex = 2 + exponentLength
            val modulusLength = ((data[modulusLengthIndex].toInt() and 0xFF) shl 8) or
                    (data[modulusLengthIndex + 1].toInt() and 0xFF)
            val modulusStartIndex = modulusLengthIndex + 2
            val modulusBytes = data.copyOfRange(modulusStartIndex, modulusStartIndex + modulusLength)
            val exponent = BigInteger(1, exponentBytes)
            val modulus = BigInteger(1, modulusBytes)
            val publicKeySpec = RSAPublicKeySpec(modulus, exponent)
            val keyFactory = KeyFactory.getInstance("RSA")
            val initialPublicKey = keyFactory.generatePublic(publicKeySpec)
            val x509EncodedKeySpec = X509EncodedKeySpec(initialPublicKey.encoded)
            keyFactory.generatePublic(x509EncodedKeySpec)
        } catch (e: Exception) {
            null
        }
    }

    fun accuracy(signature: ByteArray?, publicKey: PublicKey?, code: String): Boolean {
        return try {
            val verifier = Signature.getInstance("SHA1withRSA")
            verifier.initVerify(publicKey)
            verifier.update(code.toByteArray())
            verifier.verify(signature)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            false
        } catch (e: SignatureException) {
            e.printStackTrace()
            false
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
            false
        }
    }
}