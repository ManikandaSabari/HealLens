import os

file_path = r'c:\Users\Kunguma Varshini\OneDrive\Desktop\pdd\js\i18n.js'

with open(file_path, 'rb') as f:
    content = f.read()

# Try to decode as utf-8, if fails try latin-1
try:
    text = content.decode('utf-8')
except UnicodeDecodeError:
    text = content.decode('latin-1')

# The problematic line usually looks like this based on the grep
# We look for the "noHistory" followed by "error:" on the same line in the 'hi' section
bad_line_part = 'noHistory: "??? ?? ??? ????? ?????? ????? ????? ???? ????    error: "'

# Since the terminal shows ???, let's use a regex or a simpler search
import re
# Look for noHistory followed by error: without a closing quote and comma
fixed_text = re.sub(r'noHistory:\s*"[^"]*error:\s*"[^"]*",', 
                    'noHistory: "अभी तक कोई स्कैन इतिहास नहीं है। एक छवि स्कैन करके शुरू करें!",\n    error: "त्रुटि हुई। कृपया फिर से प्रयास करें।",', 
                    text)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(fixed_text)

print("File fixed successfully.")
