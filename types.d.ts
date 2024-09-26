/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This TypeScript modules definition file is generated by vaadin-maven-plugin.
// You can not directly import your different static files into TypeScript,
// This is needed for TypeScript compiler to declare and export as a TypeScript module.
// It is recommended to commit this file to the VCS.
// You might want to change the configurations to fit your preferences
declare module '*.css' {
    import { CSSResultGroup } from 'lit';
    const content: CSSResultGroup;
    export default content;
}declare module '*.css?inline' {
  import type { CSSResultGroup } from 'lit';
  const content: CSSResultGroup;
  export default content;
}

declare module 'csstype' {
  interface Properties {
    [index: `--${string}`]: any;
  }
}
