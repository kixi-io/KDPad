package io.kixi.apps.kdpad

import io.kixi.kd.antlr.KDLexer
import org.antlr.v4.runtime.CharStreams
import java.awt.Color
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
// import javax.swing.text.SimpleAttributeSet
import javax.swing.text.Style
import javax.swing.text.StyleConstants

/**
 * A text component with highlighting using an ANTLR Lexer
 *
 * TODO: Incremental updates (currently each edit lexes the document)
 * TODO: Error recovery
 */
class CodePane : JTextPane() {

    val defaultStyle: Style

    companion object {
        val DARK_RED = Color(168, 30, 44)
        val DARK_GOLD = Color(168, 146, 49)
        val DARK_BLUE = Color(58, 144, 182)
        val DARK_PURPLE = Color(134, 95, 157)
        val DARK_CHARTREUSE = Color(109, 148, 61)
        val DARK_GREEN = Color(54, 145, 82)
    }

    // This should all be in the companion object but makeStyle needs a reference to an
    // instance of the text component
    // TODO: refactor
    val ID = makeStyle("ID")
    var STRING = makeStyle("string", DARK_GREEN)
    var BRACKET_PAREN = makeStyle("BRACKET_PAREN", DARK_PURPLE, true)
    var NUMBER = makeStyle("NUMBER", DARK_BLUE)
    val VERSION =  makeStyle("VERSION", DARK_CHARTREUSE)
    val LITERAL =  makeStyle("LITERAL", Color.DARK_GRAY, true)
    val LITERAL_ALT =  makeStyle("LITERAL_ALT", DARK_PURPLE)
    val DATE_TIME =  makeStyle("DATE_TIME", DARK_PURPLE)
    val TIME = makeStyle("TIME", DARK_GOLD)
    val ANNOTATION = makeStyle("ANNOTATION", DARK_GOLD, true)
    val SYMBOL_OP = makeStyle("SYMBOL_OP", Color.GRAY, true)
    val ERROR = makeStyle("ERROR", DARK_RED)
    val COMMENT = makeStyle("LINE_COMMENT", Color.GRAY)

    init {
        defaultStyle =  this.makeStyle("DEFAULT")
        StyleConstants.setFontFamily(defaultStyle, "SourceCodePro-Regular")
        StyleConstants.setFontSize(defaultStyle, 16)
        document.addDocumentListener(KDDocumentListener())
    }

    fun makeStyle(name: String, color: Color? = null, bold: Boolean = false, italic: Boolean = false) : Style {
        val style = addStyle(name, defaultStyle)
        if(color!=null)
            StyleConstants.setForeground(style, color);

        if(bold) StyleConstants.setBold(style, true)
        if(italic) StyleConstants.setBold(style, true)

        return style
    }

    fun getStyle(tokenType: Int) = when(tokenType) {
        KDLexer.ID -> ID
        KDLexer.NULL, KDLexer.TRUE, KDLexer.FALSE -> LITERAL
        KDLexer.SimpleString, KDLexer.RawString, KDLexer.CharLiteral,
        KDLexer.BlockStringStart, KDLexer.BlockStringChunk, KDLexer.BlockStringEnd,
        KDLexer.BlockRawStringStart, KDLexer.BlockRawStringChunk, KDLexer.BlockRawStringEnd,
        KDLexer.BlockRawAltStringStart, KDLexer.BlockRawAltStringChunk, KDLexer.BlockRawAltStringEnd -> STRING
        KDLexer.IntegerLiteral, KDLexer.HexLiteral, KDLexer.BinLiteral, KDLexer.LongLiteral,
        KDLexer.DoubleLiteral, KDLexer.FloatLiteral, KDLexer.DecimalLiteral,
        KDLexer.BLOB_START, KDLexer.BLOB_DATA, KDLexer.BLOB_END -> NUMBER
        KDLexer.OPEN, KDLexer.CLOSE, KDLexer.LPAREN, KDLexer.RPAREN,
        KDLexer.LSQUARE, KDLexer.RSQUARE -> BRACKET_PAREN
        KDLexer.URL, KDLexer.DecimalQuantityLiteral, KDLexer.IntegerQuantityLiteral -> LITERAL_ALT
        KDLexer.Version -> VERSION
        KDLexer.Date, KDLexer.TimeZone, KDLexer.CompoundDuration, KDLexer.NanosecondDuration,
        KDLexer.MillisecondDuration, KDLexer.SecondDuration, KDLexer.MinuteDuration, KDLexer.HourDuration,
        KDLexer.DayDuration -> DATE_TIME
        KDLexer.Time -> TIME
        KDLexer.AT -> ANNOTATION
        KDLexer.COMMA, KDLexer.SEMICOLON, KDLexer.COLON, KDLexer.EQUALS,
        KDLexer.ExclusiveRangeOp, KDLexer.InclusiveRangeOp,
        KDLexer.ExclusiveLeftOp, KDLexer.ExclusiveRightOp -> SYMBOL_OP // TODO: Change name
        KDLexer.LineComment -> COMMENT
        KDLexer.BlockComment -> COMMENT
        KDLexer.WHITESPACE, KDLexer.WS -> defaultStyle
            else -> ERROR
    }

    fun colorize(lexer: KDLexer) {
        var vocab = lexer.vocabulary
        var doc = styledDocument

        var tokens = lexer.allTokens
        for(token in tokens) {
            val doHighlight = Runnable {
                doc.setCharacterAttributes(token.startIndex, token.text.length, getStyle(token.type), true)
            }
            SwingUtilities.invokeLater(doHighlight)
        }
    }

    inner class KDDocumentListener() : DocumentListener {
        var newline = "\n"
        override fun insertUpdate(e: DocumentEvent) {
            updateLog(e, "inserted into")
        }

        override fun removeUpdate(e: DocumentEvent) {
            updateLog(e, "removed from")
        }

        override fun changedUpdate(e: DocumentEvent?) {
            //Plain text components do not fire these events
        }

        fun updateLog(e: DocumentEvent, action: String) {
            // val doc: Document = e.getDocument() as Document
            // val changeLength: Int = e.getLength()
            colorize(KDLexer(CharStreams.fromString(text)))
        }
    }
}

