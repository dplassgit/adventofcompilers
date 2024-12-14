# Advent of Compilers

In which I write the compiler from "Writing a C Compiler" by Nora Sandler.

## Running

To run:

```
./mycc example/main.c
```

Requires classfiles to be in the `bin` directory

## Testing

From the `writing-a-c-compiler-tests` repo directory:

```
./test_compiler ../adventofcompilers/scripts/mycc --chapter 1
```

or 

```
./test_compiler ../adventofcompilers/scripts/mycc --chapter 3 --stage lex
```

## Notes to self

Update `TackyCodeGen` for `--stage tacky` updates.

Update `TackyInstructionToInstructionsVisitor` for `--stage codegen` updates.

Update `TackyToAsmCodeGen` for "Assembly Generation" updates.
