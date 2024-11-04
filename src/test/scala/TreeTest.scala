import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues.{convertEitherToValuable, convertLeftProjectionToValuable}
import org.scalatest.matchers.should.Matchers.{should, shouldBe, startWith}

class TreeTest extends AnyFreeSpec {
  "Tree.parse" - {
    "should parse a valid tree string with simple identifiers" in {
      val treeString = "((a bb) ccc ddd)"
      val expectedTree = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )

      val parseResult = Tree.parse(treeString)
      parseResult.value shouldBe expectedTree
    }

    "should parse a valid tree string with any identifiers matching [a-zA-Z0-9]+ regex" in {
      val treeStr = "((A bb) 3 (4d (5Ee)))"
      val expectedTree = Node(
        Node(ID("A") :: ID("bb") :: Nil) ::
          ID("3") ::
          Node(ID("4d") :: Node(ID("5Ee") :: Nil) :: Nil) ::
          Nil
      )


      val parseResult = Tree.parse(treeStr)
      parseResult.value shouldBe expectedTree
    }

    "should parse a string consisting of only an identifier" in {
      val treeStr = "a"
      val expectedTree = ID("a")
      val parseResult = Tree.parse(treeStr)

      parseResult.value shouldBe expectedTree
    }

    "should parse a string consisting of only an empty node" in {
      val treeStr = "()"
      val expectedTree = Node(Nil)
      val parseResult = Tree.parse(treeStr)

      parseResult.value shouldBe expectedTree
    }

    "should return an error when parsing an empty string" in {
      val treeStr = ""
      val parseResult = Tree.parse(treeStr)

      parseResult.left.value shouldBe "Unexpected end of input"
    }

    "should return an error when parsing a string with an identifier outside a node if the tree doesn't consist of only one identifier" in {
      val treeStr = "(a) bb"
      val parseResult = Tree.parse(treeStr)

      parseResult.left.value should startWith("Unexpected token")
    }

    "should return an error when parsing a string without a matching closing parenthesis" in {
      val treeStr = "((a bb)"
      val parseResult = Tree.parse(treeStr)

      parseResult.left.value shouldBe "Unexpected end of input"
    }

    "should return an error when parsing a string with an extra parenthesis" in {
      val treeStr = "(a bb))"
      val parseResult = Tree.parse(treeStr)

      parseResult.left.value should startWith("Unexpected token")
    }

    "should return an error when parsing a string with an identifier not matching [a-zA-Z0-9]+ regex" in {
      val treeStr = "((a bb) 3_3 ddd)"
      val parseResult = Tree.parse(treeStr)
      parseResult.left.value shouldBe "Unexpected token: '3_3'"
    }
  }
  "Tree.toString" - {
    "should return a valid string representation of an identifier" in {
      val expectedTreeString = "a"
      val tree = ID("a")

      tree.toString shouldBe expectedTreeString
    }
    "should return a valid string representation of an empty node" in {
      val expectedTreeString = "()"
      val tree = Node(Nil)

      tree.toString shouldBe expectedTreeString
    }
    "should return a valid string representation of a tree" in {
      val expectedTreeString = "((a bb) ccc ddd)"
      val tree = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
    }
  }
  "Tree.equals" - {
    "should return true when comparing two identical identifiers" in {
      val id1 = ID("a")
      val id2 = ID("a")

      (id1 == id2) shouldBe true
    }

    "should return true when comparing two identical trees" in {
      val tree1 = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
      val tree2 = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )

      (tree1 == tree2) shouldBe true
    }

    "should return false when comparing two trees with different identifiers" in {
      val tree1 = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
      val tree2 = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("CCC") ::
          ID("ddd") ::
          Nil
      )

      (tree1 == tree2) shouldBe false
    }

    "should return false when comparing two trees with different identifier order" in {
      val tree1 = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
      val tree2 = Node(
        Node(ID("bb") :: ID("a") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )

      (tree1 == tree2) shouldBe false
    }
    "should return false when comparing a single identifier and a tree with the same identifier inside a node" in {
      val tree1 = ID("a")
      val tree2 = Node(ID("bb") :: Nil)

      (tree1 == tree2) shouldBe false
    }

    "should return false when comparing trees with the same identifiers but different depth" in {
      val tree1 = Node(ID("a") :: Nil)
      val tree2 = Node(Node(ID("a") :: Nil) :: Nil)

      (tree1 == tree2) shouldBe false
    }


  }
  "Tree.replace" - {
    "should return a tree with a given node replaced by another tree" in {
      val tree = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
      val searchTree = Node(ID("a") :: ID("bb") :: Nil)
      val replacement = Node(ID("a") :: ID("bb") :: ID("eee") :: Nil)
      val expectedTree = Node(
        Node(ID("a") :: ID("bb") :: ID("eee") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )

      val newTree = Tree.replace(tree, searchTree, replacement)
      newTree shouldBe expectedTree
    }

    "should return a tree with a given identifier replaced by another tree" in {
      val tree = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
      val searchTree = ID("bb")
      val replacement = Node(ID("e") :: ID("ff") :: Nil)
      val expectedTree = Node(
        Node(ID("a") :: Node(ID("e") :: ID("ff") :: Nil) :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )

      val newTree = Tree.replace(tree, searchTree, replacement)
      newTree shouldBe expectedTree
    }

    "should replace all occurrences of the searched tree in a tree" in {
      val tree = Node(
        Node(ID("a") :: Node(ID("a") :: ID("bb") :: Nil) :: Nil) ::
          ID("ccc") ::
          ID("a") ::
          Nil
      )
      val searchTree = ID("a")
      val replacement = ID("eee")
      val expectedTree = Node(
        Node(ID("eee") :: Node(ID("eee") :: ID("bb") :: Nil) :: Nil) ::
          ID("ccc") ::
          ID("eee") ::
          Nil
      )

      val newTree = Tree.replace(tree, searchTree, replacement)
      newTree shouldBe expectedTree
    }

    "should not modify the original tree when replacing a node" in {
      val tree = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
      val expectedOriginalTree = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
      val searchTree = Node(ID("a") :: ID("bb") :: Nil)
      val replacement = Node(ID("a") :: ID("bb") :: ID("eee") :: Nil)
      val _ = Tree.replace(tree, searchTree, replacement)
      tree shouldBe expectedOriginalTree
    }

    "should not modify the original tree when replacing an identifier" in {
      val tree = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
      val expectedOriginalTree = Node(
        Node(ID("a") :: ID("bb") :: Nil) ::
          ID("ccc") ::
          ID("ddd") ::
          Nil
      )
      val searchTree = ID("bb")
      val replacement = Node(ID("e") :: ID("ff") :: Nil)

      val _ = Tree.replace(tree, searchTree, replacement)
      tree shouldBe expectedOriginalTree
    }
  }
}
