export function localize(message, object: object = {}) { return format(message, object)};

function format(string: string, arg: object = {}) {
    return Object
        .keys(arg)
        .reduce((result: string, current: string) => {
            return result.replace('{' + current + '}', (arg as any)[current]);
        }, string);
}