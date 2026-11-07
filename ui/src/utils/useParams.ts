import { useParams as useReactRouterParams } from 'react-router-dom';

export function useParams<T extends object>(): T {
  return useReactRouterParams() as T;
}
