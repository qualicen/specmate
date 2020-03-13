
export function replaceClass(oldClass: string, newClass: string): void {
    if (oldClass === newClass) {
        return;
    }
    const elements = document.getElementsByClassName(oldClass);
    while (elements.length > 0) {
        const element = elements[0];
        element.classList.add(newClass);
        element.classList.remove(oldClass);
    }
}
