import sqlite3

def export_to_sql():
    conn = sqlite3.connect('interview_mentor.db')
    cursor = conn.cursor()
    
    with open('supabase_setup.sql', 'w', encoding='utf-8') as f:
        f.write("-- Supabase Database Setup Script\n\n")
        f.write("-- 1. Create Questions Table\n")
        f.write('''CREATE TABLE IF NOT EXISTS questions (
    id SERIAL PRIMARY KEY,
    category TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    question_text TEXT NOT NULL,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_answer TEXT NOT NULL,
    explanation TEXT,
    tags TEXT
);

''')

        f.write("-- 2. Insert Data\n")
        cursor.execute("SELECT category, difficulty, question_text, option_a, option_b, option_c, option_d, correct_answer, explanation, tags FROM questions")
        rows = cursor.fetchall()
        
        if rows:
            f.write("INSERT INTO questions (category, difficulty, question_text, option_a, option_b, option_c, option_d, correct_answer, explanation, tags) VALUES\n")
            
            values = []
            for row in rows:
                formatted_row = []
                for val in row:
                    if val is None:
                        formatted_row.append("NULL")
                    else:
                        # Escape single quotes
                        escaped_val = str(val).replace("'", "''")
                        formatted_row.append(f"'{escaped_val}'")
                values.append("(" + ", ".join(formatted_row) + ")")
            
            f.write(",\n".join(values) + ";\n")
        else:
            f.write("-- No data found in SQLite questions table.\n")

    print("Successfully created supabase_setup.sql")

if __name__ == '__main__':
    export_to_sql()
