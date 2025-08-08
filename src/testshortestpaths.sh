#!/bin/bash

file=ShortestPaths.java

if [ ! -f "$file" ]; then
    echo -e "Error: File '$file' not found.\nTest failed."
    exit 1
fi

MAXTIME="0.5"
num_right=0
total=0
line="________________________________________________________________________"
compiler=
interpreter=
language=
extension=${file##*.}
if [ "$extension" = "py" ]; then
    if [ ! -z "$PYTHON_PATH" ]; then
        interpreter=$(which python.exe)
    else
        interpreter=$(which python3.2)
    fi
    command="$interpreter $file"
    echo -e "Testing $file\n"
elif [ "$extension" = "java" ]; then
    language="java"
    command="java ${file%.java}"
    echo -n "Compiling $file..."
    javac $file
    echo -e "done\n"
elif [ "$extension" = "c" ] || [ "$extension" = "cpp" ]; then
    language="c"
    command="./${file%.*}"
    echo -n "Compiling $file..."
    results=$(make 2>&1)
    if [ $? -ne 0 ]; then
        echo -e "\n$results"
        exit 1
    fi
    echo -e "done\n"
fi

run_test_args() {
    (( ++total ))
    echo -n "Running test $total..."
    expected=$2
    expected_return_val=$3
    local ismac=0
    date --version >/dev/null 2>&1
    if [ $? -ne 0 ]; then
       ismac=1
    fi
    local start=0
    if (( ismac )); then
        start=$(python3 -c 'import time; print(time.time())')
    else
        start=$(date +%s.%N)
    fi
    $command $1 2>&1 | tr -d '\r' > tmp.txt
    retval=${PIPESTATUS[0]}
    local end
    if (( ismac )); then
        end=$(python3 -c 'import time; print(time.time())')
    else
        end=$(date +%s.%N)
    fi
    received=$(cat tmp.txt)
    local elapsed=$(echo "scale=3; $end - $start" | bc | awk '{printf "%.3f", $0}')
    if (( $(echo "$elapsed > $MAXTIME" | bc -l) )); then
        echo -e "failure [timeout after $MAXTIME seconds]\n"
    elif [ "$expected" != "$received" ]; then
        echo -e "failure\n\nExpected$line\n$expected\n"
        echo -e "Received$line\n$received\n"
    else
        if [ "$expected_return_val" = "$retval" ]; then
            echo "success [$elapsed seconds]"
            (( ++num_right ))
        else
            echo "failure Return value is $retval, expected $expected_return_val."
        fi
    fi
    rm -f tmp.txt graph.txt
}

# Test 1
run_test_args "" "Usage: java ShortestPaths <filename>" "1"

# Test 2
run_test_args "notfound.txt" "Error: Cannot open file 'notfound.txt'." "1"

# Test 3
(cat << ENDOFTEXT
0
X Y 1
Y Z 4
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices '0' on line 1." "1"
rm -f graph.txt

# Test 4
(cat << ENDOFTEXT
three
X Y 1
Y Z 4
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid number of vertices 'three' on line 1." "1"
rm -f graph.txt

# Test 5
(cat << ENDOFTEXT
4
A C 2
A D 10
B A 7
B C
B D 1
C B 3
D A 2
D B 6
D C 6
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge data 'B C' on line 5." "1"
rm -f graph.txt

# Test 6
(cat << ENDOFTEXT
3
X Y 1
Y Z 4
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex 'X' on line 2 is not among valid values A-C." "1"
rm -f graph.txt

# Test 7
(cat << ENDOFTEXT
4
A C 2
A D 10
B A 7
B C 5
b D 1
C B 3
D A 2
D B 6
D C 6
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex 'b' on line 6 is not among valid values A-D." "1"
rm -f graph.txt

# Test 8
(cat << ENDOFTEXT
4
A C 2
A D 10
B A 7
B C 5
Columbia NYU 1
C B 3
D A 2
D B 6
D C 6
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Starting vertex 'Columbia' on line 6 is not among valid values A-D." "1"
rm -f graph.txt

# Test 9
(cat << ENDOFTEXT
6
A C 2
A . 10
B A 7
B C 5
B D 1
C B 3
D A 2
D B 6
D C 6
E F 8
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex '.' on line 3 is not among valid values A-F." "1"
rm -f graph.txt

# Test 10
(cat << ENDOFTEXT
4
A C 2
A D 10
B A 7
B C 5
B D 1
C B 3
D A 2
D B 6
D NYC 6
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Ending vertex 'NYC' on line 10 is not among valid values A-D." "1"
rm -f graph.txt

# Test 11
(cat << ENDOFTEXT
4
A C 2
A D 10
B A -7
B C 5
B D 1
C B 3
D A 2
D B 6
D C 6
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight '-7' on line 4." "1"
rm -f graph.txt

# Test 12
(cat << ENDOFTEXT
4
A C 2
A D 10
B A 7
B C 5
B D 1
C B 3
D A 0
D B 6
D C 6
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight '0' on line 8." "1"
rm -f graph.txt

# Test 13
(cat << ENDOFTEXT
4
A C 2
A D 10
B A 7
B C 5
B D 1
C B 3
D A 2
D B sixteen
D C 6
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Error: Invalid edge weight 'sixteen' on line 9." "1"
rm -f graph.txt

# Test 14
(cat << ENDOFTEXT
1
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" "Distance matrix:"$'\n'"  A"$'\n'"A 0"$'\n'$'\n'"Path lengths:"$'\n'"  A"$'\n'"A 0"$'\n'$'\n'"Intermediate vertices:"$'\n'"  A"$'\n'"A -"$'\n'$'\n'"A -> A, distance: 0, path: A" "0"
rm -f graph.txt

# Test 15
(cat << ENDOFTEXT
4
A C 2
A D 10
B A 7
B C 5
B D 1
C B 3
D A 2
D B 6
D C 6
ENDOFTEXT
) > graph.txt
run_test_args "graph.txt" $'Distance matrix:\n   A  B  C  D\nA  0  ∞  2 10\nB  7  0  5  1\nC  ∞  3  0  ∞\nD  2  6  6  0\n\nPath lengths:\n  A B C D\nA 0 5 2 6\nB 3 0 5 1\nC 6 3 0 4\nD 2 6 4 0\n\nIntermediate vertices:\n  A B C D\nA - C - C\nB D - - -\nC D - - B\nD - - A -\n\nA -> A, distance: 0, path: A\nA -> B, distance: 5, path: A -> C -> B\nA -> C, distance: 2, path: A -> C\nA -> D, distance: 6, path: A -> C -> B -> D\nB -> A, distance: 3, path: B -> D -> A\nB -> B, distance: 0, path: B\nB -> C, distance: 5, path: B -> C\nB -> D, distance: 1, path: B -> D\nC -> A, distance: 6, path: C -> B -> D -> A\nC -> B, distance: 3, path: C -> B\nC -> C, distance: 0, path: C\nC -> D, distance: 4, path: C -> B -> D\nD -> A, distance: 2, path: D -> A\nD -> B, distance: 6, path: D -> B\nD -> C, distance: 4, path: D -> A -> C\nD -> D, distance: 0, path: D' "0"
rm -f graph.txt

echo -e "\nTotal tests run: $total"
echo -e "Number correct : $num_right"
echo -n "Percent correct: "
echo "scale=2; 100 * $num_right / $total" | bc

if [ "$language" = "java" ]; then
    echo -e -n "\nRemoving class files..."
    rm -f *.class
    echo "done"
elif [ $language = "c" ]; then
    echo -e -n "\nCleaning project..."
    make clean > /dev/null 2>&1
    echo "done"
fi