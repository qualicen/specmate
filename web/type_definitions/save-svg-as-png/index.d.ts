declare module "save-svg-as-png" {
  export function prepareSvg(el: any, options?: any, done?: any): void;
  export function svgAsDataUri(el: any, options?: any): Promise<string>;
  export function svgAsPngUri(el: any, options?: any): Promise<string>;
  export function saveSvg(el: any, name: string, options?: any): void;
  export function saveSvgAsPng(el: any, name: string, options?: any): void;
}
