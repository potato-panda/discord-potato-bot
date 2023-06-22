export interface TwitterError {
  errors: Error[];
}

export interface Error {
  code:    number;
  message: string;
}
