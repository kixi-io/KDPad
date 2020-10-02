package io.kixi.app.kdpad

import io.kixi.kd.KD
import java.awt.BorderLayout
import java.awt.EventQueue
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.net.URL
import javax.swing.*

/**
 * KPad is a simple app that allows you to enter KD on the left, click "Eval" and see
 * the the tag tree on the right.
 *
 * Coming soon: Syntax highlighting and a Kotlin REPL console on the bottom
 */
class KDPad : JFrame() {

    val codeSplitter = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
    val windowSplitter = JSplitPane(JSplitPane.VERTICAL_SPLIT)

    var codePane = CodePane()
    var komPane = CodePane()
    var outputArea = OutputArea()

    init {
        title = "KD Pad: sample.kd"
        komPane.isEditable = false

        layoutStage()

        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(900, 600)
        codeSplitter.dividerLocation=390
        windowSplitter.dividerLocation=480

        System.setOut(outputArea.outStream)
        System.setErr(outputArea.errStream)

        setLocationRelativeTo(null)

        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        ge.registerFont(
            Font.createFont(
                Font.TRUETYPE_FONT,
                readResource("fonts/SourceCodePro-Regular.ttf").openStream()
            )
        )
        val font = Font("SourceCodePro-Regular", Font.PLAIN, 16)

        codePane.font = font
        komPane.font = font
        outputArea.font = font
    }

    private fun layoutStage() {
        val codeScroller = JScrollPane(codePane)
        codeScroller.border = null

        val komScroller = JScrollPane(komPane)
        komScroller.border = null

        codeSplitter.leftComponent = codeScroller
        codeSplitter.rightComponent = komScroller

        val outputScroller = JScrollPane(outputArea)
        outputScroller.border = null

        windowSplitter.topComponent = codeSplitter
        windowSplitter.bottomComponent = outputScroller

        // We can't use a triple quote block because Kotlin, unlike Swift and C#, does
        // not provide a way to escape embedded trible quotes
        codePane.text = readResource("example.kd").readText()

        add(windowSplitter)

        val toolbar = JPanel(BorderLayout())
        toolbar.setBorder(BorderFactory.createEmptyBorder(2,2,2,2))
        val evalButton = JButton("Eval")
        evalButton.addActionListener{ eval() }

        toolbar.add(evalButton, BorderLayout.EAST)
        add(toolbar, BorderLayout.NORTH)
    }

    private fun eval() {
        try {
            komPane.text = KD.read(codePane.text).toString()
        } catch(e: Throwable) {
            e.printStackTrace(outputArea.errStream)
        }
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
