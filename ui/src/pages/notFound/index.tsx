import asset_not_found_png from 'images/not-found.png';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import Button from 'components/Button';
import SwitchLanguage from 'components/SwitchLanguage';
import { getRedirectUrl } from 'utils/getRedirectUrl';
import { EventTrack } from 'utils/track';
import styles from './index.module.scss';

const NotFound = () => {
  const navigate = useNavigate();
  const [ redirectUrl, setRedirectUrl ] = useState<string>('');
  const location = useLocation();

  useEffect(() => {
    EventTrack.pageView(location.pathname);
  }, [location.pathname]);

  const handleGoHome = useCallback(() => {
    navigate(redirectUrl);
  }, [navigate, redirectUrl]);

  const init = useCallback(async() => {
    const redirectUrl = await getRedirectUrl('/notfound');
    setRedirectUrl(redirectUrl);
  }, []);

  useEffect(() => {
    init();
  }, [init]);

	return (
		<div className={styles['not-found']}>
      <div className={styles['switch-language']}>
        <SwitchLanguage />
      </div>
      <div className={styles.content}>
        <div className={styles.image}>
          <img src={asset_not_found_png}  alt='404' />
        </div>
        <div className={styles.description}>
          <div className={styles.title}>404</div>
          <div>
            <FormattedMessage id='notfound.description' />
          </div>
          <div className={styles.btn}>
            <Button onClick={handleGoHome} primary>
              <FormattedMessage id='notfound.button' />
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NotFound;
