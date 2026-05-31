import json
import os

file_path = r'c:\Users\Kunguma Varshini\OneDrive\Desktop\pdd\js\scanner.js'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Try to see if it's a quoted string
if content.startswith('"') and content.endswith('"'):
    try:
        # Decode the escaped characters
        decoded = json.loads(content)
        with open(r'c:\Users\Kunguma Varshini\OneDrive\Desktop\pdd\js\scanner_fixed.js', 'w', encoding='utf-8') as f_out:
            f_out.write(decoded)
        print("Successfully decoded to scanner_fixed.js")
    except Exception as e:
        print(f"Error decoding: {e}")
        # Try a different approach if it's not a valid JSON string
        try:
            # Maybe it's just raw text with \n literals
            decoded = content.encode('utf-8').decode('unicode_escape')
            with open(r'c:\Users\Kunguma Varshini\OneDrive\Desktop\pdd\js\scanner_fixed.js', 'w', encoding='utf-8') as f_out:
                f_out.write(decoded)
            print("Successfully decoded using unicode_escape to scanner_fixed.js")
        except Exception as e2:
            print(f"Error decoding with unicode_escape: {e2}")
else:
    print("Content does not start and end with quotes.")
    # Still try unicode_escape
    try:
        decoded = content.encode('utf-8').decode('unicode_escape')
        with open(r'c:\Users\Kunguma Varshini\OneDrive\Desktop\pdd\js\scanner_fixed.js', 'w', encoding='utf-8') as f_out:
            f_out.write(decoded)
        print("Successfully decoded using unicode_escape (no quotes) to scanner_fixed.js")
    except Exception as e:
        print(f"Final error: {e}")
