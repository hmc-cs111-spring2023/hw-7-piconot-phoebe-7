# Design

## Who is the target for this design, e.g., are you assuming any knowledge on the part of the language users?
The target of this design is to mimic language used by regular people to direct someone across a space. I am not assuming much technical knowledge of the users - the script should look like someone reading a list of directions or rules to navigate someone who is blind(folded).

## Why did you choose this design, i.e., why did you think it would be a good idea for users to express the maze-searching computation using this syntax?
I chose this design because this wording is the way I understood Picobot in my head along with the way the lecture slides describe the maze algorithm. If it took the instructors explaining it this way for easier understanding, then it could also make for better syntax for Picobot maze-navigation.

## What behaviors are easier to express in your design than in Picobot’s original design?  If there are no such behaviors, why not?
The movement of Picobot is easier to express in my design than in Picobot because it is more explicitly stated, rather than hidden within a rule. 


## What behaviors are more difficult to express in your design than in Picobot’s original design? If there are no such behaviors, why not?
Because of the wording centers directionality to Picobot locally (rather than "global" directions from an omnipresent view), it is harder to create sets of rules where the components of a rule do not correlate with each other in some way. In other words, state is fixed to the 4 relative directions (left/right/up/down). Surroundings and cardinal directions have to be implicitly derived from those directions. 

## On a scale of 1–10 (where 10 is “very different”), how different is your syntax from PicoBot’s original design?
I consider the difference to be around 6-7. I still have ideas of state with a similar alphabet, but surroundings is more internalized to Picobot and rules (definitions) are altered because of this internalization.

## Is there anything you would improve about your design?
I have not thought about comments as much in this design. Additionally, I could add directions when "step" is called to de-correlate components of a rule a bit more.