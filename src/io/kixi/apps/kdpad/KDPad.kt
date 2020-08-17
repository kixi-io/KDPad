package io.kixi.apps.kdpad

import io.kixi.kd.KD
import java.awt.BorderLayout
import java.awt.EventQueue
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.net.URL
import javax.swing.*

class KDPad : JFrame() {

    val splitter = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
    var codePane = JTextPane()
    var komPane = JTextPane()

    init {
        setTitle("KD Pad")

        layoutStage()

        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(900, 600)
        splitter.dividerLocation=350

        setLocationRelativeTo(null)

        var ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        ge.registerFont(
            Font.createFont(
                Font.TRUETYPE_FONT,
                readResource("fonts/SourceCodePro-Regular.ttf").openStream()
            )
        )
        val font = Font("SourceCodePro-Regular", Font.PLAIN, 16)

        codePane.font = font
        komPane.font = font
    }

    private fun layoutStage() {
        val codeScroller = JScrollPane(codePane)
        codeScroller.border = null
        val komScroller = JScrollPane(komPane)
        komScroller.border = null

        splitter.leftComponent = codeScroller
        splitter.rightComponent = komScroller

        // We can't use a triple quote block because Kotlin, unlike Swift and C#, does
        // not provide a way to escape embedded trible quotes
        codePane.text = "# Tag w/ a bare String & attribute\n\n" +
                        "test Foo nums=[1 2 3]\n\n" +
                        "# Swift-style String block\n\n" +
                        "text \"\"\"\n" +
                        "     Lorem \"ipsum\"\n" +
                        "         dolor sit\n" +
                        "     amet\n" +
                        "     \"\"\"\n\n" +
                        "# Raw string & line continuation\n\n" +
                        "lib @\"\\libs\\KD.jar\" \\\n" +
                        "    version=5.2-beta-5"


        add(splitter)

        var toolbar = JPanel(BorderLayout())
        var evalButton = JButton("Eval")
        evalButton.addActionListener({
            eval()
        })

        toolbar.add(evalButton, BorderLayout.EAST)
        add(toolbar, BorderLayout.NORTH)
    }

    private fun eval() {
        var kdCode = codePane.text
        komPane.text = KD.read(kdCode).toString()
    }

    companion object {
        fun readResource(resource: String): URL =
            this::class.java.getResource("/" + resource)
    }
}

private fun createAndShowGUI() {
    val frame = KDPad()
    frame.isVisible = true
}

fun main(args : Array<String>) = EventQueue.invokeLater(::createAndShowGUI)
