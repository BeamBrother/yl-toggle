import { useEffect, useCallback, useState, Suspense, useMemo } from 'react';
import { createBrowserRouter, Navigate, Outlet, RouterProvider } from 'react-router-dom';
import { headerRoutes, blankRoutes } from './routes';
import { getApplicationSettings } from 'services/application';
import Loading from 'components/Loading';
import { getRedirectUrl } from 'utils/getRedirectUrl';
import BasicLayout from 'layout/BasicLayout';
import { EventTrack } from 'utils/track';
import { IApplicationSetting } from 'interfaces/applicationSetting';

const Router = () => {
  const [redirectUrl, setRedirectUrl] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const initRedirectUrl = useCallback(async () => {
    if (window.location.pathname === '/login') {
      return;
    }
    const nextRedirectUrl = await getRedirectUrl('/notfound');
    setRedirectUrl(nextRedirectUrl);
  }, []);

  useEffect(() => {
    EventTrack.init();
  }, []);

  const init = useCallback(async () => {
    const res = await getApplicationSettings<IApplicationSetting>();

    if (res.success && res.data && res.data.loginMode === 'GUEST') {
      localStorage.setItem('isDemo', 'true');
    } else {
      localStorage.setItem('isDemo', 'false');
    }
    setIsLoading(false);
    initRedirectUrl();
  }, [initRedirectUrl]);

  useEffect(() => {
    init();
  }, [init]);

  const router = useMemo(
    () =>
      createBrowserRouter([
        ...blankRoutes.map((route) => ({
          path: route.path,
          element: <route.component />,
        })),
        {
          path: '/',
          element: (
            <BasicLayout>
              <Outlet />
            </BasicLayout>
          ),
          children: [
            ...headerRoutes.map((route) => ({
              path: route.path,
              element: <route.component />,
            })),
            {
              path: '/',
              element: <Navigate to={redirectUrl || '/notfound'} replace />,
            },
            {
              path: '*',
              element: <Navigate to={redirectUrl || '/notfound'} replace />,
            },
          ],
        },
      ]),
    [redirectUrl]
  );

  if (isLoading) {
    return <Loading />;
  }

  return (
    <Suspense fallback={<Loading />}>
      <RouterProvider router={router} />
    </Suspense>
  );
};

export default Router;
