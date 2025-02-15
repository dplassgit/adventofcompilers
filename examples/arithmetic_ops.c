/* Test basic arithmetic operations on unsigned integers
 * None of these operations wrap around; that's tested separately in arithmetic_wraparound
 */

unsigned int ui_a;
unsigned int ui_b;

int division(void) {
    // ui_a = 100
    // ui_b = 4294967294

    /* ui_a/ui_b is 0.
     * If you interpreted these as signed values, ui_b would be -2
     * and ui_a / ui_b would be -50
     */
    return (ui_a / ui_b == 0);
}

int main(void) {

    ui_a = 100u;
    ui_b = 4294967294u;

    if (!division()) {
        return 4;
    }

    return 0;
}
