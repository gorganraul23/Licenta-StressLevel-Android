package book.kotlinforandroid.hr.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import book.kotlinforandroid.hr.R
import book.kotlinforandroid.hr.Utils
import book.kotlinforandroid.hr.databinding.ActivityConfigBinding
import org.w3c.dom.Document
import java.io.File
import java.io.FileNotFoundException
import java.net.InetAddress
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ConfigActivity : Activity() {

    private val APP_TAG = "ConfigActivity"
    private lateinit var binding: ActivityConfigBinding
    private lateinit var ipAddressInput: EditText
    private lateinit var continueBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Utils.clearEmail()
        Utils.userId = 0

        // finding the Continue button
        continueBtn = findViewById(R.id.btnContinue)
        // finding the edit text ipAddress
        ipAddressInput = findViewById(R.id.IpInput)

        // Setting On Click Listener
        continueBtn.setOnClickListener {
            val ipAddress = ipAddressInput.text.toString()

            //if(isIpReachable(ipAddress)){
                Toast.makeText(this@ConfigActivity, "IP Address Found", Toast.LENGTH_SHORT).show()
                Utils.ipAddress = ipAddress
                //updateDomainInXml("app/src/main/res/xml/network_security_config.xml", Utils.ipAddress)
                val intent = Intent(this@ConfigActivity, LoginActivity::class.java)
                startActivity(intent)
//            }
//            else{
//                Toast.makeText(this@ConfigActivity, "Invalid IP Address", Toast.LENGTH_SHORT).show()
//            }
        }
    }

    private fun isIpReachable(ip: String): Boolean {
        return try {
            val inet = InetAddress.getByName(ip)
            inet.isReachable(1000) // Timeout in milliseconds
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun updateDomainInXml(filePath: String, newIpAddress: String) {
        try {
            // Load the XML file
            val xmlFile = File(filePath)
            if (!xmlFile.exists()) {
                throw FileNotFoundException("XML file not found at $filePath")
            }

            // Parse the XML document
            val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val document: Document = documentBuilder.parse(xmlFile)
            document.documentElement.normalize()

            // Find the <domain> element
            val domainElements = document.getElementsByTagName("domain")
            if (domainElements.length > 0) {
                val domainNode = domainElements.item(0) // Assumes there's only one <domain> node
                domainNode.textContent = newIpAddress // Update the text content
                println("Domain updated to: $newIpAddress")
            } else {
                println("No <domain> element found in the XML.")
                return
            }

            // Write changes back to the file
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.transform(DOMSource(document), StreamResult(xmlFile))
            println("XML file updated successfully.")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error updating XML file: ${e.message}")
        }
    }
}