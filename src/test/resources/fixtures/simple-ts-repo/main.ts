import { external } from './external';

export function greet(name: string): string {
  return `Hello ${name}`;
}

export function run(callback: () => void): string {
  callback();
  return greet('world');
}

export class Greeter {
  say(): string {
    return greet('friend');
  }
}
