package pl.msitko.xml.optics

import monocle.function.Plated
import pl.msitko.xml.BasicSpec
import pl.msitko.xml.entities.{LabeledElement, ResolvedName, Text}
import pl.msitko.xml.optics.XmlDocumentOptics.rootLens
import pl.msitko.xml.printing.PrinterConfig
import pl.msitko.xml.test.utils.ExampleBuilderHelper

trait PlatedSpec extends BasicSpec with ExampleBuilderHelper {
  import OpticsInstances._

  implicit val printerConfig = PrinterConfig.Default

  def testPlated(plated: LabeledElement => LabeledElement, input: String, expectedOutput: String) = {
    val res = rootLens.modify(plated)(parse(input))
    print(res) should === (expectedOutput)
  }

  "nodePlated" should {
    "just test" in testPlated(
      Plated.transform[LabeledElement] {
        LabeledElementOptics.allTexts.composeIso(TextOptics.textIso).modify(_.toUpperCase)
      },
      input1, output1
    )

    "be able to transform all nodes with `f` label" in testPlated(
      Plated.transform[LabeledElement] {
        case el: LabeledElement if el.label == ResolvedName.unprefixed("f") =>
          LabeledElementOptics.children.set(List(Text("something")))(el)
        case element => element
      },
      input2, output2
    )

    "be able to transform all `f` labels to `xyz`" in testPlated(
      Plated.transform[LabeledElement] {
        case el: LabeledElement if el.label == ResolvedName.unprefixed("f") =>
          el.copy(label = ResolvedName.unprefixed("xyz"))
        case element => element
      },
      input2, output3
    )

    "be able to transform all `f` labels to `xyz` (even at top level)" in testPlated(
      Plated.rewrite[LabeledElement] {
        case el: LabeledElement if el.label == ResolvedName.unprefixed("f") =>
          Some(el.copy(label = ResolvedName.unprefixed("xyz")))
        case _ => None
      },
      input4, output4
    )

    // TODO: there's no possibility to compose Optics with Plated as deep, deeper and so on have `Element` as a target
    // and
    "be able to transform text of all `f` elements in selected, known subtree" ignore {
      testPlated(
        Plated.transform[LabeledElement] {
          case el: LabeledElement if el.label == ResolvedName.unprefixed("f") =>
            el.copy(label = ResolvedName.unprefixed("xyz"))
          case element => element
        },
        input5, output5
      )
    }
    // we should provide facade that take care of that
  }

  val input1 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<a>
      |   <c1>
      |      <f>item1</f>
      |      <g>item2</g>
      |   </c1>
      |   <c1>
      |      <f>item1</f>
      |      <h>another item</h>
      |   </c1>
      |</a>""".stripMargin

  val output1 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<a>
      |   <c1>
      |      <f>ITEM1</f>
      |      <g>ITEM2</g>
      |   </c1>
      |   <c1>
      |      <f>ITEM1</f>
      |      <h>ANOTHER ITEM</h>
      |   </c1>
      |</a>""".stripMargin

  val input2 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<a><f>something</f><c1><f>item1</f><g>item2</g></c1><c2><f>item1<f></f></f><h>another item<f></f></h><f><a>item3</a></f></c2></a>""".stripMargin

  val output2 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<a><f>something</f><c1><f>something</f><g>item2</g></c1><c2><f>something</f><h>another item<f>something</f></h><f>something</f></c2></a>""".stripMargin

  val output3 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<a><xyz>something</xyz><c1><xyz>item1</xyz><g>item2</g></c1><c2><xyz>item1<xyz></xyz></xyz><h>another item<xyz></xyz></h><xyz><a>item3</a></xyz></c2></a>""".stripMargin

  val input4 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<f><f>something</f><c1><f>item1</f><g>item2</g></c1><c2><f>item1<f></f></f><h>another item<f></f></h><f><a>item3</a></f></c2></f>""".stripMargin

  val output4 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<xyz><xyz>something</xyz><c1><xyz>item1</xyz><g>item2</g></c1><c2><xyz>item1<xyz></xyz></xyz><h>another item<xyz></xyz></h><xyz><a>item3</a></xyz></c2></xyz>""".stripMargin

  val input5 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<a>
      |   <c1>
      |      <f>item</f>
      |      <g>item</g>
      |   </c1>
      |   <b>
      |     <f>item</f>
      |     <g>item</g>
      |     <c1>
      |       <f>item</f>
      |       <g>item</g>
      |     </c1>
      |   </b>
      |   <c>
      |     <c1>
      |        <f>item</f>
      |        <h>item</h>
      |     </c1>
      |   </c>
      |</a>""".stripMargin

  val output5 =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<a>
      |   <c1>
      |      <f>item</f>
      |      <g>item</g>
      |   </c1>
      |   <b>
      |     <f>ITEM</f>
      |     <g>item</g>
      |     <c1>
      |       <f>ITEM</f>
      |       <g>item</g>
      |     </c1>
      |   </b>
      |   <c>
      |     <c1>
      |        <f>item</f>
      |        <h>item</h>
      |     </c1>
      |   </c>
      |</a>""".stripMargin
}
