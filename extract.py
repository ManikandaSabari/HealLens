import json
import re

log_file = r'C:\Users\Kunguma Varshini\.gemini\antigravity\brain\f952f363-f3bd-4d04-976e-9d62ce510907\.system_generated\logs\overview.txt'

with open(log_file, 'r', encoding='utf-8') as f:
    lines = f.readlines()

out_dir = r'C:\Users\Kunguma Varshini\.gemini\antigravity\brain\9b002822-3b19-4e63-b224-e48238e2147b\scratch'

files_state = {}

for line in lines:
    try:
        data = json.loads(line)
    except:
        continue
    
    # Check if this is a view_file response
    if data.get('source') == 'USER_EXPLICIT' and data.get('type') == 'VIEW_FILE':
        content = data.get('content', '')
        # Check for line-numbered content
        if "The following code has been modified" in content:
            lines_content = content.split('\n')
            filepath = None
            for cl in lines_content:
                if cl.startswith('File Path: `file:///'):
                    filepath = cl.replace('File Path: `file:///', '').replace('`', '').replace('%20', ' ')
                    break
            if filepath:
                filepath = filepath.lower()
                if filepath not in files_state:
                    files_state[filepath] = {}
                for cl in lines_content:
                    m = re.match(r'^(\d+):\s(.*)$', cl)
                    if m:
                        lnum = int(m.group(1))
                        lcontent = m.group(2)
                        files_state[filepath][lnum] = lcontent

    # We also want to apply replace_file_content! Wait, replace_file_content is in PLANNER_RESPONSE tool_calls
    if data.get('source') == 'MODEL' and data.get('type') == 'PLANNER_RESPONSE':
        tool_calls = data.get('tool_calls', [])
        for tool in tool_calls:
            if tool.get('name') == 'replace_file_content':
                args = tool.get('args', {})
                tf = args.get('TargetFile', '').lower().replace('\\', '/')
                start = int(args.get('StartLine', 0))
                end = int(args.get('EndLine', 0))
                repl = args.get('ReplacementContent', '')
                # To apply replace_file_content, we need the full file! But we only have a dictionary of line numbers.
                # Actually, reconstructing from replace_file_content is extremely hard without a library that handles it exactly as the system does.
                
for fpath, ldict in files_state.items():
    print(f"Extracted views for {fpath}: {len(ldict)} lines")

