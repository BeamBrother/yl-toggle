import { useCallback } from 'react';
import classNames from 'classnames';
import { useIntl } from 'react-intl';
import { useNavigate } from 'react-router-dom';
import { APITOKEN_PATH, MEMBER_PATH, WEBHOOK_LIST_PATH } from 'router/routes';
import { useMatchedRoutePath } from 'utils/router';
import PutAwayMemu from 'components/PutAwayMenu';
import { WEBHOOKLIST, MEMBERS, TOKENS } from 'constants/sidebar';
import { SidebarContainer } from './hooks';
import styles from './sidebar.module.scss';

const Sidebar = () => {
  const { isPutAway } = SidebarContainer.useContainer();
  const navigate = useNavigate();
  const matchedPath = useMatchedRoutePath([MEMBER_PATH, WEBHOOK_LIST_PATH, APITOKEN_PATH]);
  const intl = useIntl();

  const sidebarCls = classNames(
    styles['setting-sidebar'],
    {
      [styles['setting-sidebar-close']]: isPutAway
    }
  );

  const memberCls = classNames(
    styles['project-menu-item'],
    {
      [styles['project-menu-item-close']]: isPutAway,
    },
    {
      [styles.selected]: matchedPath === MEMBER_PATH,
    }
  );

  const webhookCls = classNames(
    styles['project-menu-item'],
    {
      [styles['project-menu-item-close']]: isPutAway,
    },
    {
      [styles.selected]: matchedPath === WEBHOOK_LIST_PATH,
    }
  );

  const tokenCls = classNames(
    styles['project-menu-item'],
    {
      [styles['project-menu-item-close']]: isPutAway,
    },
    {
      [styles.selected]: matchedPath === APITOKEN_PATH,
    }
  );

  const gotoPage = useCallback((path: string) => {
    navigate(`/settings/${path}`);
  }, [navigate]);

  return (
    <div className={sidebarCls}>
      <div className={memberCls} onClick={() => gotoPage(MEMBERS)}>
        <PutAwayMemu
          type='member'
          isPutAway={isPutAway}
          title={intl.formatMessage({id: 'common.members.text'})}
        />
      </div>
      <div className={webhookCls} onClick={() => gotoPage(WEBHOOKLIST)}>
        <PutAwayMemu
          type='webhook'
          isPutAway={isPutAway}
          title={intl.formatMessage({id: 'common.webhooks.text'})}
        />
      </div>
      <div className={tokenCls} onClick={() => gotoPage(TOKENS)}>
        <PutAwayMemu
          type="token"
          isPutAway={isPutAway}
          title={intl.formatMessage({ id: 'common.tokens.text' })} />
      </div>
    </div>
	);
};

export default Sidebar;
