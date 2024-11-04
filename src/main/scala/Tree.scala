/**
 * An abstract type representing a TL tree.
 * Concrete implementations are [[Node]] and [[ID]].
 */
sealed trait Tree:
  override def toString: String

/**
 * A class representing a TL node.
 * @param children the list of children of the node
 */
case class Node(children: List[Tree]) extends Tree:
  override def toString: String = children.mkString("(", " ", ")")

/**
 * A class representing a TL identifier.
 * @param name the name of the identifier
 */
case class ID(name: String) extends Tree:
  override def toString: String = name

object ID:
  /** Regex used to validate identifiers. */
  private val VALID_ID_REGEX = "[a-zA-Z0-9]+"
  /**
   * Checks if a string is a valid identifier.
   * @param name the string to check
   * @return true if the string is a valid identifier, false otherwise
   */
  def isValidName(name: String): Boolean = name matches VALID_ID_REGEX

object Tree:
  /** Regex used to split tokens in a tree string. */
  private val TOKENS_SPLIT_REGEX = "\\s+|\\b|(?<=[()])"

  /**
   * Replaces all occurrences of a given tree in a tree with another tree.
   * @param tree the original tree where the search tree should be replaced
   * @param searchTree the tree to be replaced
   * @param replacement the tree that will replace the search tree
   * @return A new tree with the search tree replaced by the replacement tree
   */
  def replace(tree: Tree, searchTree: Tree, replacement: Tree): Tree =
    if tree == searchTree then replacement
    else tree match
      case Node(children) => Node(children.map { replace(_, searchTree, replacement) })
      case ID(_) => tree

  /**
   * Parses a string into a tree.
   * @param treeStr the string to parse
   * @return [[Left]] with an error message if the string is not a valid tree, [[Right]] with the parsed tree otherwise
   */
  def parse(treeStr: String): Either[String, Tree] =
    val tokens = treeStr.split(TOKENS_SPLIT_REGEX).filter(_.nonEmpty).toList
    parseTokens(tokens)
      .flatMap { (tree, remaining) => remaining match
        case Nil => Right(tree)
        case _ => Left(s"Unexpected token: '${remaining.head}'")
      }

  /**
   * Helper function parsing a list of tokens.
   * @param tokens the list of tokens to parse
   * @return [[Left]] with an error message if an unexpected token or end of input occurs,
   *         [[Right]] with the parsed tree otherwise
   */
  private def parseTokens(tokens: List[String]): Either[String, (Tree, List[String])] = tokens match
    case Nil => Left("Unexpected end of input")
    case "(" :: rest =>
      parseNode(rest)
        .map { (children, rest2) => (Node(children), rest2) }
    case name :: rest if ID isValidName name =>
      Right(ID(name), rest)
    case token :: _ =>
      Left(s"Unexpected token: '$token'")

  /**
   * Helper function parsing a list of tokens into a node.
   * @param tokens the list of tokens to parse
   * @return [[Left]] with an error message if an unexpected token or end of input occurs,
   *         [[Right]] with the parsed node otherwise
   */
  private def parseNode(tokens: List[String]): Either[String, (List[Tree], List[String])] = tokens match
    case ")" :: rest => Right(Nil, rest)
    case _ =>
      parseTokens(tokens)
        .flatMap { (child, rest2) =>
          parseNode(rest2)
            .map { (children, rest3) => (child :: children, rest3) }
        }

