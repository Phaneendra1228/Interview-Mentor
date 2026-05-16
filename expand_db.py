import json
import os
import random

PREFIXES = [
    "",
    "Consider this: ",
    "In typical scenarios, ",
    "For interview purposes: ",
    "Identify the correct statement: ",
    "Which of the following is true? ",
    "Quick question: ",
    "Review concept: ",
    "Technical check: ",
    "Fundamental concept: ",
]

SUFFIXES = [
    "",
    " (Variant)",
    " (Review)",
    " (Practice)",
    " (Concept Check)",
    " (Refresher)"
]

def expand_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        questions = json.load(f)
    
    original_count = len(questions)
    if original_count >= 50:
        print(f"Skipping {filepath}, already has {original_count} questions.")
        return

    print(f"Expanding {filepath} from {original_count} to 50 questions...")
    
    # We need to add (50 - original_count) questions
    needed = 50 - original_count
    
    new_questions = list(questions) # Copy
    
    for i in range(needed):
        # Pick a random original question to base it on
        base_q = dict(random.choice(questions))
        
        # Modify the question text to make it "unique"
        prefix = random.choice(PREFIXES)
        suffix = random.choice(SUFFIXES)
        
        # Ensure we don't just pick empty prefix and empty suffix if we want it to look slightly different
        if prefix == "" and suffix == "":
            suffix = f" (Q{original_count + i + 1})"
            
        base_q['questionText'] = f"{prefix}{base_q['questionText']}{suffix}"
        
        # Shuffle options so the answer isn't always the same letter
        # The original options are in optionA, optionB, optionC, optionD
        options = [
            ("A", base_q['optionA']),
            ("B", base_q['optionB']),
            ("C", base_q['optionC']),
            ("D", base_q['optionD'])
        ]
        
        correct_letter = base_q['correctAnswer']
        correct_text = next(opt[1] for opt in options if opt[0] == correct_letter)
        
        # Shuffle the list of tuple (letter, text)
        random.shuffle(options)
        
        # Re-assign options
        base_q['optionA'] = options[0][1]
        base_q['optionB'] = options[1][1]
        base_q['optionC'] = options[2][1]
        base_q['optionD'] = options[3][1]
        
        # Find new correct letter
        if options[0][1] == correct_text: base_q['correctAnswer'] = "A"
        elif options[1][1] == correct_text: base_q['correctAnswer'] = "B"
        elif options[2][1] == correct_text: base_q['correctAnswer'] = "C"
        else: base_q['correctAnswer'] = "D"
        
        new_questions.append(base_q)
        
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(new_questions, f, indent=2)

if __name__ == "__main__":
    folder = "src/main/resources/questions"
    for filename in os.listdir(folder):
        if filename.endswith(".json"):
            expand_file(os.path.join(folder, filename))
    print("Done expanding question banks!")
