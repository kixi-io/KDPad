package io.kixi.app.kdpad

import java.awt.BorderLayout
import java.awt.Color
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.text.BadLocationException
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants


class OutputArea : JPanel() {

    companion object {
        private val OUT_STYLE = SimpleAttributeSet()
        private val ERR_STYLE = SimpleAttributeSet()
        private val ERR_COLOR = Color(190, 30, 35)

        init {
            StyleConstants.setFontFamily(OUT_STYLE, "Source Code Pro")
            StyleConstants.setFontSize(OUT_STYLE, 14)
            StyleConstants.setForeground(OUT_STYLE, Color.BLACK)
            StyleConstants.setFontFamily(ERR_STYLE, "Source Code Pro")
            StyleConstants.setFontSize(ERR_STYLE, 14)
            StyleConstants.setForeground(ERR_STYLE, ERR_COLOR)
        }
    }

    private val scrollPane: JScrollPane
    private var textPane: JTextPane = JTextPane()
    val outStream: PrintStream
    val errStream: PrintStream

    fun clear() {
        textPane!!.text = ""
    }

    inner class StyledOutputStream(var attributeSet: SimpleAttributeSet) : OutputStream() {
        @Throws(IOException::class)
        override fun write(b: Int) {
            SwingUtilities.invokeLater {
                try {
                    val doc = textPane!!.styledDocument
                    doc.insertString(doc.length, b.toChar().toString(), attributeSet)
                    textPane!!.caretPosition = doc.length
                } catch (e: BadLocationException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
        }
    }

    init {
        scrollPane = JScrollPane(textPane)
        scrollPane.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        textPane!!.border = EmptyBorder(6, 8, 0, 0)
        // textPane.setFont(sourceCodePro);
        setLayout(BorderLayout())
        add(scrollPane)
        outStream = PrintStream(StyledOutputStream(OUT_STYLE))
        errStream = PrintStream(StyledOutputStream(ERR_STYLE))
    }
}
