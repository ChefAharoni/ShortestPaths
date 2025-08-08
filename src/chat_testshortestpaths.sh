#!/bin/bash
# Extended edge-case tests for ShortestPaths
# - Adds 10+ new tests
# - Writes expected/received outputs under ./diffs
# - Echoes PASS/FAIL summary

set -u

FILE="ShortestPaths.java"
CLASS="ShortestPaths"
DIFFDIR="diffs"
MAXTIME="0.5"

# Choose a timeout command if present (Linux: timeout, macOS with coreutils: gtimeout)
if command -v timeout >/dev/null 2>&1; then
  TIMEOUT="timeout ${MAXTIME}"
elif command -v gtimeout >/dev/null 2>&1; then
  TIMEOUT="gtimeout ${MAXTIME}"
else
  TIMEOUT=""  # no timeout available
fi

# Compile
if [ ! -f "$FILE" ]; then
  echo -e "Error: File '$FILE' not found.\nTest failed."
  exit 1
fi

echo -n "Compiling $FILE..."
if ! javac "$FILE" >/dev/null 2>&1; then
  echo -e " failed\n"
  echo "Compilation failed. Aborting."
  exit 1
fi
echo " done."

mkdir -p "$DIFFDIR"

num_right=0
total=0

run_case_with_file () {
  # Args: id, description, input_content, expected_content
  local id="$1"
  local desc="$2"
  local input_content="$3"
  local expected_content="$4"

  local in_file="case_${id}.txt"
  local exp_file="${DIFFDIR}/expected_${id}.txt"
  local got_file="${DIFFDIR}/received_${id}.txt"

  printf "%s" "$input_content" > "$in_file"
  printf "%s" "$expected_content" > "$exp_file"

  total=$((total+1))

  if [ -n "$TIMEOUT" ]; then
    ${TIMEOUT} java "$CLASS" "$in_file" > "$got_file" 2>&1 || true
  else
    java "$CLASS" "$in_file" > "$got_file" 2>&1 || true
  fi

  if diff -u "$exp_file" "$got_file" >/dev/null 2>&1; then
    echo "PASS  [$id] $desc"
    num_right=$((num_right+1))
  else
    echo "FAIL  [$id] $desc"
    echo "  See: $exp_file  vs  $got_file"
  fi

  rm -f "$in_file"
}

run_case_no_args () {
  # Args: id, description, expected_content
  local id="$1"
  local desc="$2"
  local expected_content="$3"
  local exp_file="${DIFFDIR}/expected_${id}.txt"
  local got_file="${DIFFDIR}/received_${id}.txt"
  printf "%s" "$expected_content" > "$exp_file"

  total=$((total+1))

  if [ -n "$TIMEOUT" ]; then
    ${TIMEOUT} java "$CLASS" > "$got_file" 2>&1 || true
  else
    java "$CLASS" > "$got_file" 2>&1 || true
  fi

  if diff -u "$exp_file" "$got_file" >/dev/null 2>&1; then
    echo "PASS  [$id] $desc"
    num_right=$((num_right+1))
  else
    echo "FAIL  [$id] $desc"
    echo "  See: $exp_file  vs  $got_file"
  fi
}

run_case_two_args () {
  # Args: id, description, arg1, arg2, expected_content
  local id="$1"
  local desc="$2"
  local arg1="$3"
  local arg2="$4"
  local expected_content="$5"
  local exp_file="${DIFFDIR}/expected_${id}.txt"
  local got_file="${DIFFDIR}/received_${id}.txt"
  printf "%s" "$expected_content" > "$exp_file"

  total=$((total+1))

  if [ -n "$TIMEOUT" ]; then
    ${TIMEOUT} java "$CLASS" "$arg1" "$arg2" > "$got_file" 2>&1 || true
  else
    java "$CLASS" "$arg1" "$arg2" > "$got_file" 2>&1 || true
  fi

  if diff -u "$exp_file" "$got_file" >/dev/null 2>&1; then
    echo "PASS  [$id] $desc"
    num_right=$((num_right+1))
  else
    echo "FAIL  [$id] $desc"
    echo "  See: $exp_file  vs  $got_file"
  fi
}

# ---------- EDGE CASES ----------

# NEW01: No arguments (usage)
run_case_no_args "NEW01" "No arguments -> usage message" "Usage: java ShortestPaths <filename>
"

# NEW02: Too many arguments
run_case_two_args "NEW02" "Two arguments -> usage message" "a.txt" "b.txt" "Usage: java ShortestPaths <filename>
"

# NEW03: File does not exist
exp="Error: Cannot open file 'notfound.txt'."
echo "$exp" > "${DIFFDIR}/expected_NEW03.txt"
total=$((total+1))
if [ -n "$TIMEOUT" ]; then
  ${TIMEOUT} java "$CLASS" "notfound.txt" > "${DIFFDIR}/received_NEW03.txt" 2>&1 || true
else
  java "$CLASS" "notfound.txt" > "${DIFFDIR}/received_NEW03.txt" 2>&1 || true
fi
if diff -u "${DIFFDIR}/expected_NEW03.txt" "${DIFFDIR}/received_NEW03.txt" >/dev/null 2>&1; then
  echo "PASS  [NEW03] Missing file"
  num_right=$((num_right+1))
else
  echo "FAIL  [NEW03] Missing file"
  echo "  See: ${DIFFDIR}/expected_NEW03.txt  vs  ${DIFFDIR}/received_NEW03.txt"
fi

# NEW04: Vertex count not an integer
run_case_with_file "NEW04" "Non-integer vertex count" "X
A B 1
" "Error: Invalid number of vertices 'X' on line 1.
"

# NEW05: Vertex count 0 (out of range)
run_case_with_file "NEW05" "Vertex count 0 (out of range)" "0
A B 1
" "Error: Invalid number of vertices '0' on line 1.
"

# NEW06: Invalid edge data (only two components)
run_case_with_file "NEW06" "Invalid edge data; two components" "3
A B
" "Error: Invalid edge data 'A B' on line 2.
"

# NEW07: Lowercase vertex name (start)
run_case_with_file "NEW07" "Lowercase starting vertex" "3
a B 5
" "Error: Starting vertex 'a' on line 2 is not among valid values A-C.
"

# NEW08: Non-integer weight (e.g., 3.14)
run_case_with_file "NEW08" "Non-integer weight" "3
A B 3.14
" "Error: Invalid edge weight '3.14' on line 2.
"

# NEW09: Zero weight (invalid; must be positive)
run_case_with_file "NEW09" "Zero weight is invalid" "3
A B 0
" "Error: Invalid edge weight '0' on line 2.
"

# NEW10: MAX_INT weight handled as a real edge, not infinity
read -r -d '' input_NEW10 << 'EOF'
2
A B 2147483647
EOF

read -r -d '' expected_NEW10 << 'EOF'
Distance matrix:
           A          B
A          0 2147483647
B          ∞          0

Path lengths:
           A          B
A          0 2147483647
B          ∞          0

Intermediate vertices:
  A B
A - -
B - -

A -> A, distance: 0, path: A
A -> B, distance: 2147483647, path: A -> B
B -> A, distance: infinity, path: none
B -> B, distance: 0, path: B
EOF

run_case_with_file "NEW10" "MAX_INT weight should print as a number, not ∞" "$input_NEW10" "$expected_NEW10"

# NEW11: Two vertices, no edges (reachability/infinity + none paths)
read -r -d '' input_NEW11 << 'EOF'
2
EOF

read -r -d '' expected_NEW11 << 'EOF'
Distance matrix:
  A B
A 0 ∞
B ∞ 0

Path lengths:
  A B
A 0 ∞
B ∞ 0

Intermediate vertices:
  A B
A - -
B - -

A -> A, distance: 0, path: A
A -> B, distance: infinity, path: none
B -> A, distance: infinity, path: none
B -> B, distance: 0, path: B
EOF

run_case_with_file "NEW11" "Two vertices, no edges -> infinity/none cases" "$input_NEW11" "$expected_NEW11"

# NEW12: Duplicate edge overrides previous
read -r -d '' input_NEW12 << 'EOF'
2
A B 10
A B 3
EOF

read -r -d '' expected_NEW12 << 'EOF'
Distance matrix:
  A B
A 0 3
B ∞ 0

Path lengths:
  A B
A 0 3
B ∞ 0

Intermediate vertices:
  A B
A - -
B - -

A -> A, distance: 0, path: A
A -> B, distance: 3, path: A -> B
B -> A, distance: infinity, path: none
B -> B, distance: 0, path: B
EOF

run_case_with_file "NEW12" "Duplicate edges: last one wins" "$input_NEW12" "$expected_NEW12"

# NEW13: Max allowed vertices (26), followed by an invalid edge line
read -r -d '' input_NEW13 << 'EOF'
26
A AA 1
EOF
run_case_with_file "NEW13" "26 vertices + invalid ending vertex token" "$input_NEW13" "Error: Ending vertex 'AA' on line 2 is not among valid values A-Z.
"

# NEW14: Trailing/leading whitespace tolerated but still validated
read -r -d '' input_NEW14 << 'EOF'
3
   A      C     2
B      A   -7
EOF
run_case_with_file "NEW14" "Whitespace variations + invalid negative weight" "$input_NEW14" "Error: Invalid edge weight '-7' on line 3.
"

echo
echo "===== SUMMARY ====="
echo "Total tests: $total"
echo "Passed:      $num_right"
echo "Failed:      $((total - num_right))"
