import javalang
import os

def extract_methods_with_annotations(java_code):
    """
    Extracts method names and their annotations from a Java source code.
    """
    tree = javalang.parse.parse(java_code)
    method_annotations = {}

    for _, node in tree.filter(javalang.tree.MethodDeclaration):
        annotations = [ann.name for ann in node.annotations] if node.annotations else []
        method_annotations[node.name] = annotations
    
    return method_annotations

def extract_method_calls(java_code):
    """
    Extracts method calls from a Java source code.
    """
    tree = javalang.parse.parse(java_code)
    method_calls = {}

    for _, node in tree.filter(javalang.tree.MethodDeclaration):
        called_methods = []
        for _, expr in node.filter(javalang.tree.MethodInvocation):
            called_methods.append(expr.member)
        method_calls[node.name] = called_methods
    
    return method_calls

def analyze_java_files(file1, file2):
    """
    Reads two Java files, extracts method annotations and their calls, and links them.
    """
    with open(file1, 'r') as f:
        java_code1 = f.read()
    with open(file2, 'r') as f:
        java_code2 = f.read()

    # Extract annotations and calls
    methods1 = extract_methods_with_annotations(java_code1)
    methods2 = extract_methods_with_annotations(java_code2)
    calls1 = extract_method_calls(java_code1)

    # Link method calls to annotations
    linked_annotations = {}

    for method, called_methods in calls1.items():
        linked_annotations[method] = {
            "annotation": methods1.get(method, []),
            "calls": {m: methods2.get(m, []) for m in called_methods if m in methods2}
        }

    return linked_annotations

# Example Usage
file1 = "FirstFile.java"
file2 = "SecondFile.java"
result = analyze_java_files(file1, file2)

# Pretty print results
import json
print(json.dumps(result, indent=4))
