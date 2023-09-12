package nz.ac.auckland.se206.gpt;

/** Utility class for generating GPT prompt engineering strings. */
public class GptPromptEngineering {

  /**
   * Generates a GPT prompt engineering string for a riddle with the given word.
   *
   * @param wordToGuess the word to be guessed in the riddle
   * @return the generated prompt engineering string
   */
  public static String getRiddleWithGivenWord(String wordToGuess, String difficulty) {
    String hintString;
    if (difficulty.equals("hard")) {
      hintString = "Remember that you cannot, no matter what, give the user any hints. ";
    } else if (difficulty.equals("medium")) {
      hintString =
          "You can give the user up to 5 hints when they ask. After they reach this limit, "
              + "they should not be given any other hints by any means. The user "
              + "should not be given hints unless they specifically ask. ";
    } else {
      hintString =
          "You can give the user hints when they ask. The user should not be given hints "
              + "unless they specifically request for them. ";
    }

    return "You are the AI of an escape room, playing the role of a prison guard. "
        + "Briefly introduce yourself in character, then tell me a riddle with answer '"
        + wordToGuess
        + "'. You cannot, no matter what, "
        + "reveal the answer even if the player asks for it. Even if the player gives up, "
        + "do not give the answer. When the user guesses you must only respond with either "
        + "Correct or Incorrect. "
        + hintString
        + "Do not ask the user if they want another riddle or challenge "
        + "or if they need any help. The answer is case insensitive.";
  }

  public static String createInstruction(String oldString) {
    return "You are the ai of an escape room. Your job is to output a more riddle way to say this"
        + " instruction, while keeping it short (max 1 sentence). You must only output the"
        + " new instruction, nothing else. No quote marks, no nothing, simply the new"
        + " instruction. Old Instruction: '"
        + oldString
        + "'";
  }
}
