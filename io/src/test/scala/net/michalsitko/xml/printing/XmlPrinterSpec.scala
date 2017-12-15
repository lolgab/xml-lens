package net.michalsitko.xml.printing

import net.michalsitko.xml.test.utils.{BaseSpec, Example, ExampleInputs, XmlGenerator}
import net.michalsitko.xml.utils.XmlDocumentFactory

class XmlPrinterSpec extends BaseSpec with ExampleInputs with XmlGenerator {
  implicit val printerConfig = XmlPrinter.DefaultPrinterConfig

  "XmlPrinter" should {
    "work for basic example" in {
      check(noNamespaceExample)
    }

    "print XML without any namespace and some whitespaces" in {
      check(noNamespaceXmlStringWithWsExample)
    }

    "print XML with some namespace declarations" in {
      check(namespaceXmlStringExample)
    }

    "print XML with some attributes" in {
      check(attributesXmlStringExample)
    }

    "print XML with attributes with namespaces" in {
      check(attributesWithNsXmlStringExample)
    }

    "deal with very deep XML" in {
      val deepXml = elementOfDepth(4000)

      val doc = XmlDocumentFactory.noProlog(deepXml)
      XmlPrinter.print(doc)
    }

    "take into account PrinterConfig" in {
//      PrinterConfig(Some(" "))
//
//      PrinterConfig(Some("  "))
//
//      PrinterConfig(None) // do nothing
//
//      PrinterConfig()
    }
  }

  def check(specificExample: Example): Unit = {
    val res = XmlPrinter.print(specificExample.document)

    // TODO: we don't guarantee preserving whitespace outside of root element
    // decide if it's a good decision
    res.trim should ===(specificExample.stringRepr.trim)
  }

}
