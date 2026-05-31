import re

log_file = r'C:\Users\Kunguma Varshini\.gemini\antigravity\brain\9b002822-3b19-4e63-b224-e48238e2147b\scratch\all_outputs.txt'

with open(log_file, 'r', encoding='utf-8') as f:
    lines = f.readlines()

current_file = None
files_data = {}

# Match line number and content
# Format: "123: content"
line_regex = re.compile(r'^(\d+):\s(.*)$')
# Match File Path: `file:///c:/...`
file_regex = re.compile(r'File Path:\s*`file:///(.*?)`', re.IGNORECASE)

for i, line in enumerate(lines):
    # Check for file path
    m = file_regex.search(line)
    if m:
        path = m.group(1).replace('%20', ' ')
        # fix windows path
        if len(path) > 2 and path[1] == ':':
            pass
        elif path.startswith('c:/'):
            pass
        current_file = path.lower()
        if current_file not in files_data:
            files_data[current_file] = {}
        continue
    
    # Extract line data
    m = line_regex.match(line)
    if m and current_file:
        line_num = int(m.group(1))
        content = m.group(2)
        # Always take the latest view of this line
        files_data[current_file][line_num] = content

# Write out the reconstructed files
out_dir = r'C:\Users\Kunguma Varshini\.gemini\antigravity\brain\9b002822-3b19-4e63-b224-e48238e2147b\scratch'

for fpath, lines_dict in files_data.items():
    if not lines_dict: continue
    max_line = max(lines_dict.keys())
    
    # We might have missing lines if they weren't viewed!
    # Let's count missing
    missing = [i for i in range(1, max_line + 1) if i not in lines_dict]
    
    # Let's write the reconstructed file to scratch
    import os
    basename = os.path.basename(fpath)
    save_path = os.path.join(out_dir, "reconstructed_" + basename)
    
    with open(save_path, 'w', encoding='utf-8') as f:
        for i in range(1, max_line + 1):
            if i in lines_dict:
                f.write(lines_dict[i] + '\n')
            else:
                f.write(f'// MISSING LINE {i}\n')
                
    print(f"Reconstructed {basename}: {max_line} lines, {len(missing)} missing.")
