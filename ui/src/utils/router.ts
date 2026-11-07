import { matchPath, useLocation } from 'react-router-dom';

export const useMatchedRoutePath = (patterns: string[]) => {
  const { pathname } = useLocation();
  return patterns.find((pattern) => matchPath({ path: pattern, end: true }, pathname)) ?? pathname;
};
