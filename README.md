# FlappyBird-MC
A Spigot plugin for FlappyBird.

Each player gets a block. By dropping it, they must find their way through the pipes, as well as to collect the 3x3 popwerups that show up around the course.

## Powerups

### Gray
Get Closer Gaps - The next gap through the pipes will be within 20 blocks of the current gap.

### Red
Move Slower - Go slower, if you need more time to get to the gap.

### Green
Move Everyone Faster - Move everyone faster, so your competition has a harder time.

### Pink
Move to the Gap - Skip up to the gap if you just can't.

## Commands

### /startgame
Starts the game. (requires flappybird.startgame)

### /powerup <1-4>
Make a powerup block in your path, with the number picking the powerup in the order of the list. (requires flappybird.powerup)

## Exposed API

### static boolean gameActive
Activates the game. Used by /startgame.

### static List<String> players
List of players, and their associated indices.

### static List<Boolean> alive
Boolean of whether or not the player at that index in players is alive.

### static int spaceAllowed
The space between pipes in the game.
