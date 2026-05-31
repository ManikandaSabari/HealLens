file_path = r'c:\Users\Kunguma Varshini\OneDrive\Desktop\pdd\js\scanner.js'
with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()
    print(f"Total length: {len(content)}")
    print("--- FIRST 500 CHARS ---")
    print(content[:500])
    print("--- LAST 500 CHARS ---")
    print(content[-500:])
