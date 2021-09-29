export function localize(message, object: object = {}) { return format(message, object)};

function format(string: string, arg: object = {}) {
    return Object
        .keys(arg)
        .reduce((first: string, second: string) => {
            return first.replace('{' + second + '}', (arg as any)[second]);
        }, string);
}