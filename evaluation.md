# Evaluation: running commentary

## Internal DSL

_Describe each change from your ideal syntax to the syntax you implemented, and
describe_ why _you made the change._

**On a scale of 1–10 (where 10 is "a lot"), how much did you have to change your syntax?**

**On a scale of 1–10 (where 10 is "very difficult"), how difficult was it to map your syntax to the provided API?**

## External DSL

_Describe each change from your ideal syntax to the syntax you implemented, and
describe_ why _you made the change._

1. Rather than a "main" function (repeat until finish), each state function will call another state function within its definition.
- Explicitly jumping between states makes parsing a lot easier, especially since States are terminal symbols (base cases)
2. Associating states per relative direction (left/right/up/down).
- I noticed that my ideal program for empty room and maze both contain four state definitions that mostly map 1-to-1 with moving in the four different directions. This simplification helped with creating fixed number of simple State terminals.
3. Remove "face x" and x: direction from "step x". Separating direction Picobot is facing from the direction the State represent is doable, but difficult to wrap my head around when I started to write parsing combinators. 
- This creates more connections between rule components, specifically the direction Picobot can move is the same as the direction represented by each state. If Picobot needs to move in other directions, it must change state.
- This removal can still create valid empty room and maze Picobot programs, just not as efficient as the sample solutions.
4. Differentiating between "step" and "stay".
- I forgot about X as a possibility for MoveDirections. Stay is meant to be the equivalent of X, while step is N/S/E/W depending on the associated direction of current state.
5. "and" to chain step/stay with a state change
- Similar to #1, it was easier to make state changes along immediately with a step when explicitly given.


**On a scale of 1–10 (where 10 is "a lot"), how much did you have to change your syntax?**
4 - It still looks more like human instruction than Picobot code.

**On a scale of 1–10 (where 10 is "very difficult"), how difficult was it to map your syntax to the provided API?**
8 - There were no 1-to-1 mappings for words in my syntax to the API for Surroundings, MoveDirection, and RelativeDescription. The need for the single mention of state to be parsed together with many while/step clauses meant Rules cannot be generated individually.