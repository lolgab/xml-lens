package pl.msitko.xml.optics.laws

import monocle.law.discipline.{LensTests, OptionalTests, TraversalTests}
import org.scalacheck.Arbitrary
import org.scalatest.Matchers
import pl.msitko.xml.entities.Node
import pl.msitko.xml.optics.ElementOptics
import pl.msitko.xml.test.utils.{ArbitraryElementConfig, ArbitraryInstances, CogenInstances}

class ElementOpticsLawsSpec extends LawsSpec with Matchers with ArbitraryInstances with CogenInstances {
  import pl.msitko.xml.entities.Instances._
  import pl.msitko.xml.optics.ElementOptics._

  import scalaz.std.string._

  implicit val arbLabeledElem = Arbitrary(labeledElementGen(ArbitraryElementConfig(4, 4, Some("abc"), None)))
  implicit val arbNode = Arbitrary(arbLabeledElem.arbitrary.map(_.asInstanceOf[Node]))
  implicit val arbElem =
    Arbitrary(labeledElementGen(ArbitraryElementConfig(1, 2, None, Some("someAttr"))).map(_.element))
  implicit val arbAttr = Arbitrary(attributeGen(Some("someAttr")))

  val deeperTest              = TraversalTests(deeper("abc"))
  val allLabeledElementsTest  = TraversalTests(allLabeledElements)
  val hasTextOnlyTest         = OptionalTests(hasTextOnly)
  val attributeTest           = OptionalTests(attribute("someAttr"))
  val hasOneChildTest         = OptionalTests(hasOneChild)
  val attributesTest          = LensTests(attributes)
  val childrenTest            = LensTests(ElementOptics.children)

  checkLaws("deeper Traversal", deeperTest)
  checkLaws("allLabeledElements Traversal", allLabeledElementsTest)
  checkLaws("hasTextOnly Optional", hasTextOnlyTest)
  checkLaws("attribute Optional", attributeTest)
  checkLaws("hasOneChild Optional", hasOneChildTest)
  checkLaws("attributes Lens", attributesTest)
  // TODO: investigate why tests are slow with default value for maxSize
  checkLaws("children Lens", childrenTest, 8)
}

