import { useCallback } from 'react';
import classNames from 'classnames';
import { useIntl } from 'react-intl';
import { useNavigate } from 'react-router-dom';
import { PERSONAL_APITOKEN_PATH, PROFILE_PATH } from 'router/routes';
import { useMatchedRoutePath } from 'utils/router';
import PutAwayMemu from 'components/PutAwayMenu';
import { PROFILE, TOKENS } from 'constants/sidebar';
import { SidebarContainer } from './hooks';
import styles from './sidebar.module.scss';

const Sidebar = () => {
  const { isPutAway } = SidebarContainer.useContainer();
  const navigate = useNavigate();
  const matchedPath = useMatchedRoutePath([PROFILE_PATH, PERSONAL_APITOKEN_PATH]);
  const intl = useIntl();

  const sidebarCls = classNames(styles['setting-sidebar'], {
    [styles['setting-sidebar-close']]: isPutAway,
  });

  const profileCls = classNames(
    styles['project-menu-item'],
    {
      [styles['project-menu-item-close']]: isPutAway,
    },
    {
      [styles.selected]: matchedPath === PROFILE_PATH,
    }
  );

  const tokenCls = classNames(
    styles['project-menu-item'],
    {
      [styles['project-menu-item-close']]: isPutAway,
    },
    {
      [styles.selected]: matchedPath === PERSONAL_APITOKEN_PATH,
    }
  );

  const gotoPage = useCallback(
    (path: string) => {
      navigate(`/user/${path}`);
    },
    [navigate]
  );

  return (
    <div className={sidebarCls}>
      <div className={profileCls} onClick={() => gotoPage(PROFILE)}>
        <PutAwayMemu type="attribute" isPutAway={isPutAway} title={intl.formatMessage({ id: 'common.profile.text' })} />
      </div>
      <div className={tokenCls} onClick={() => gotoPage(TOKENS)}>
        <PutAwayMemu type="token" isPutAway={isPutAway} title={intl.formatMessage({ id: 'common.tokens.text' })} />
      </div>
    </div>
  );
};

export default Sidebar;
