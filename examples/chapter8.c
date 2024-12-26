int main(void) {
  for(int i = 0; i < 10; i = i + 1) {
    while(i!=1) { break; }
    do { i = i + 1; } while(i < 5);
    continue;
  }
}

