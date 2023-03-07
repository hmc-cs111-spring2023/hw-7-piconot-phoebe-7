package piconot.external

import scala.util.parsing.combinator._
import picolib.semantics._
import java.lang.module.ModuleDescriptor.Opens



object PiconotParser extends JavaTokenParsers with PackratParsers {

    // #################################################################
    // Helper maps and functions
    // for creating MoveDirection, RelativeDescription, and Surroundings
    // #################################################################

    // note that direction to the right is the state as integer + 1
    val stateMoveDirectionMap: Map[State, MoveDirection] =
        Map(
            State("0") -> North,
            State("1") -> East,
            State("2") -> South,
            State("3") -> West
        )

    // helper to get surroundings as Surroundings as Picobot string "xxxx"
    val blockedMoveDirectionMap: Map[MoveDirection, String] =
        Map(
            North -> "N",
            East -> "E",
            South -> "S",
            West -> "W"
        )
    
    def getSurroundingsAsString(
        dir: MoveDirection, 
        status: String, 
        surr: String = "****"): String = {
            val charChanged = status match {
                case "blocked" => blockedMoveDirectionMap(dir)
                case "clear" => "x"
                case _ => "*"
            }
            // TODO: x's in input surr converted to N/E/W/S

            val result = dir match {
                case North => charChanged ++ surr.tail
                case East => surr.charAt(0) ++ charChanged ++ surr.substring(2)
                case South => surr.substring(0, 2) ++ charChanged ++ surr.substring(3)
                case West => surr.take(3) ++ charChanged
            }
            result
        }
    
    // convert Surrounding string to Surrounding object
    def convertSurroundingString(surr: String): Surroundings = {
        def convertRelativeDescription(c: Char): RelativeDescription = 
            c match {
                case 'x' => Open
                case '*' => Anything
                _ => Blocked 
            }


        Surroundings(
            converRelativeDescription(surr(0)),
            converRelativeDescription(surr(1)),
            converRelativeDescription(surr(2)),
            converRelativeDescription(surr(3))
        )
    }

    // ############################################################
    // Parsers
    // ############################################################

    def moveLogic: Parser[List[Rule]] = 
        state ~ rep(clause) ~ state ^^ {
            case startState ~ terms ~ finalState => {
                // imperative way of storing stuff for generating rules
                var currState = startState
                var lastSurroundingString = "****"
                var rules = List.empty[Rule]

                // terms are while (queryDir) is (status) go (newDir) or
                // (queryMove) and move (newState) as 4-tuple
                terms.foreach(t => t match {
                    // ######################################
                    // (queryDir, status, newDir, None) cases
                    case (queryDir, status, newDir, None) => ""
                    case ("path", "clear", "straight", None) => {
                        val moveDir = stateMoveDirectionMap(currState)
                        val surroundingsAsString = getSurroundingsAsString(
                            moveDir, 
                            "clear", 
                            lastSurroundingString)
                
                        rules.append(Rule(
                            currState,
                            convertSurroundingString(surroundingsAsString),
                            moveDir,
                            currState
                        ))

                        lastSurroundingString = surroundingsAsString
                    }
                    case ("right", "clear", "right", None) => {
                        val nextState = State((x.toInt)+1)
                        val movDir = stateMoveDirectionMap(nextState)
                        val surroundingsAsString = getSurroundingsAsString(
                            moveDir, 
                            "clear", 
                            lastSurroundingString)
                
                        rules.append(Rule(
                            currState,
                            convertSurroundingString(surroundingsAsString),
                            moveDir,
                            nextState
                        ))

                        currState = newState
                        lastSurroundingString = surroundingsAsString
                    }

                    // #######################################
                    // (queryMove, "", "", newState) cases
                    case ("step", _, _, newState) => {
                        // we assume that step direction == state direction
                        // ex. we cannot do move right but step upwards (we must step right)
                        // this reduces the fluency provided by Picobot; one way to overcome this
                        // is to add a "face x" annotation and a "step y"; x and y are directions
                        // but that is more parsing that I do not have time for...

                        // surrounding must be clear in direction we are stepping
                        val surroundingsAsString = getSurroundingsAsString(
                            stateMoveDirectionMap(currState), 
                            "clear",
                            lastSurroundingString)


                        rules.append(Rule(
                            currState, 
                            convertSurroundingString(surroundingsAsString), 
                            stateMoveDirectionMap(currState), 
                            newState))
                        
                        currState = newState
                        lastSurroundingString = surroundingsAsString
                    }
                    case ("stay", _, _, newState) => {
                        // at least in the 2 picobot examples, we always stop when we hit a wall

                        // this is like **** or x**S
                        val surroundingsAsString = getSurroundingsAsString(
                            stateMoveDirectionMap(currState), 
                            "blocked",
                            lastSurroundingString)


                        rules.append(Rule(
                            currState, 
                            convertSurroundingString(surroundingsAsString), 
                            StayHere, 
                            newState))
                        
                        currState = newState
                        lastSurroundingString = surroundingsAsString
                    }

                    case _ => Failure("conflicting surroundings and movement")
                })

                rules
            }
        }

    // intermediate parser to cut syntactic sugar and get bits to match Picobot library
    def clause: Parser[(String, String, String, State)] =
        ("while " <~ ("path" | "right") ~> " is " ~ ("clear" | "blocked")
        ~> "go " ~ ("straight" | "right")) // while (queryDir) is (status) go (newDir)

        | (("step" | "stay") ~> " and " ~ state) // (queryMove) and move (newState)

        ^^ {
            case queryDir ~ status ~ newDir => (queryDir, status, newDir, None)
            case queryMove ~ newState => (queryMove, "", "", newState)
            case _ => Failure("unparsable while or step clause")
        }

    
    def state: Parser[State] =
        "move " <~ ("left" | "right" | "up" | "down") ^^ {
            case "up" => State("0") // north
            case "right" => State("1") // east
            case "down" => State("2") // south
            case "left" => State("3") // west
        }


        // (string representing surroundings ex: xE**, moveDirection, newstate)
    // def startMoveLogic: Parser[(String, MoveDirection, State)] = 
    //     state ~ // "move left/right/up/down"

    //     // repeat these clauses
    //     ("while " <~ ("path" | "right") ~> " is " ~ ("clear" | "blocked")
    //     ~> "go " ~ ("straight" | "right")) // while __ is __ go ___

    //     | (("step" | "stay") ~> " and " ~ state) // step and move __

        
    //     ^^ {
    //         // while path is clear run straight
    //         case currState ~ "path" ~ "clear" ~ "straight" => {
    //             val moveDir = stateMoveDirectionMap(currState)
                
                
    //             (getSurroundingsAsString(moveDir, "clear"),
    //             moveDir, 
    //             currState)
    //         }
    //         // while right is clear go right
    //         case State(x) ~ "right" ~ "clear" ~ "right" => {
    //             val nextState = State((x.toInt)+1)
    //             val movDir = stateMoveDirectionMap(nextState)

    //             (getSurroundingsAsString(moveDir, "clear"),
    //             moveDir, 
    //             nextState) 
    //         }
    //         // step and (move <direction>): state
    //         case currState ~ movement ~ newState => {
    //             case "step" => (
    //                 getSurroundingsAsString(stateMoveDirectionMap(currState), "clear"),
    //                 stateMoveDirectionMap(currState), 
    //                 newState)
    //             case "stay" => (
    //                 // at least in the 2 picobot examples, we always stop when we hit a wall
    //                 getSurroundingsAsString(stateMoveDirectionMap(currState), "blocked"),
    //                 StayHere, 
    //                 newState)
    //         }
    //     }

    // this was to place around with adding the prhase "face x" to allow stepping in
    // a different direction
        // def wdirection: Parser[RelativeDescription] =
        // "face " <~ ("north" | "south" | "east" | "west") ~>
        // "while path is clear"

        // def direction: Parser[MoveDirection] =
        //     "face " ~ ("north" | "south" | "east" | "west") ~ 
    

    // def moveLogic: Parser[List[Rule]] = 
    //     state ~ 
    //     rep(
    //     ("while " <~ ("path" | "right") ~> " is " ~ ("clear" | "blocked")
    //     ~> "go " ~ ("straight" | "right")) // while __ is __ run/go
    //     | (("step" | "stay") ~> " and " ~ state) // step and move _
    //     )
    //     ~ state ^^ {
    //         case (currSurr, moveDir, newState) ~ _____ ~ newState => {

    //         }
    //     }
}