package io.kixi.app.kdpad

import io.kixi.kd.antlr.KDLexer
import io.kixi.kd.antlr.KDParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import java.awt.Color
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
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
        val DARK_PURPLE = Color(132, 95, 155)
        val DARK_PERIWINKLE = Color(90, 115, 170)
        val DARK_BLUE = Color(62, 142, 180)
        val DARK_AQUA = Color(53, 141, 136)
        val DARK_GREEN = Color(55, 146, 76)
        val DARK_CHARTREUSE = Color(119, 138, 61)
        val DARK_GOLD = Color(168, 146, 49)
        val DARK_ORANGE = Color(168, 92, 47)
        val DARK_RED = Color(168, 30, 44)
    }

    // This should all be in the companion object but makeStyle needs a reference to an
    // instance of the text component
    // TODO: refactor
    val ID = makeStyle("ID", Color(41, 81, 120))
    val TAG_NAME = makeStyle("TAG_NAME", DARK_PERIWINKLE)
    var STRING = makeStyle("STRING", DARK_GREEN)
    var ATTRIBUTE_KEY = makeStyle("ATTRIBUTE_KEY", DARK_PURPLE, false, true)
    var BRACKET_PAREN = makeStyle("BRACKET_PAREN", DARK_PURPLE, true)
    var NUMBER = makeStyle("NUMBER", DARK_AQUA) // DARK_BLUE)
    val VERSION =  makeStyle("VERSION", DARK_CHARTREUSE)
    // val LITERAL =  makeStyle("LITERAL", Color.DARK_GRAY, true)
    val LITERAL =  makeStyle("LITERAL", DARK_ORANGE)
    val LITERAL_ALT =  makeStyle("LITERAL_ALT", DARK_PURPLE)
    val DATE_TIME =  makeStyle("DATE_TIME", DARK_PURPLE)
    val TIME = makeStyle("TIME", DARK_GOLD)
    val ANNOTATION = makeStyle("ANNOTATION", DARK_GOLD)
    val SYMBOL_OP = makeStyle("SYMBOL_OP", Color.GRAY, true)
    val ERROR = makeStyle("ERROR", DARK_RED)
    val COMMENT = makeStyle("LINE_COMMENT", Color.GRAY, italic = true)

    init {
        defaultStyle =  this.makeStyle("DEFAULT")
        StyleConstants.setFontFamily(defaultStyle, "SourceCodePro-Regular")
        StyleConstants.setFontSize(defaultStyle, 16)
        document.addDocumentListener(KDDocumentListener())
    }

    fun makeStyle(name: String, color: Color? = null, bold: Boolean = false, italic: Boolean = false) : Style {
        val style = addStyle(name, defaultStyle)
        if(color!=null)
            StyleConstants.setForeground(style, color)

        if(bold) StyleConstants.setBold(style, true)
        if(italic) StyleConstants.setItalic(style, true)

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
        KDLexer.Date, KDLexer.CompoundDuration, KDLexer.NanosecondDuration,
        KDLexer.MillisecondDuration, KDLexer.SecondDuration, KDLexer.MinuteDuration, KDLexer.HourDuration,
        KDLexer.DayDuration -> DATE_TIME
        KDLexer.Time -> TIME
        KDLexer.AT -> ANNOTATION
        KDLexer.COMMA, KDLexer.SEMICOLON, KDLexer.COLON, KDLexer.EQUALS,
        KDLexer.ExclusiveRangeOp, KDLexer.InclusiveRangeOp,
        KDLexer.ExclusiveLeftOp, KDLexer.ExclusiveRightOp -> SYMBOL_OP // TODO: Change name
        KDLexer.LineComment -> COMMENT
        KDLexer.BlockComment -> COMMENT
        KDLexer.WS -> defaultStyle
            else -> ERROR
    }


    fun colorize(lexer: KDLexer) {
        val doc = styledDocument

        val tokens = lexer.allTokens

        var lastToken: Token = tokens.get(KDParser.WS)
        for(token in tokens) {
            val doHighlight = Runnable {

                var style = getStyle(token.type)

                // Handle annotations
                if(lastToken.type == KDLexer.AT && token.type == KDLexer.ID) {
                    style = getStyle(lastToken.type)

                // Handle tag name
                } else if((lastToken.type == KDLexer.NL || lastToken.type ==  KDLexer.SEMICOLON)
                        &&  token.type == KDLexer.ID) {
                    style = TAG_NAME
                }

                doc.setCharacterAttributes(token.startIndex, token.text.length, style, true)

                // handle attribute key
                if(token.type == KDLexer.EQUALS && lastToken.type == KDLexer.ID) {
                    doc.setCharacterAttributes(lastToken.startIndex, lastToken.text.length, ATTRIBUTE_KEY, true)
                }

                if(token.type!=KDLexer.WS) {
                    lastToken = token
                }

            }
            SwingUtilities.invokeLater(doHighlight)
        }
    }

    inner class KDDocumentListener() : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) {
            updateLog(e, "inserted into")
        }

        override fun removeUpdate(e: DocumentEvent) {
            updateLog(e, "removed from")
        }

        override fun changedUpdate(e: DocumentEvent?) {
            //Plain text components do not fire these events
        }

        private fun updateLog(e: DocumentEvent, action: String) {
            // val doc: Document = e.getDocument() as Document
            // val changeLength: Int = e.getLength()
            colorize(KDLexer(CharStreams.fromString(text)))
        }
    }
}

