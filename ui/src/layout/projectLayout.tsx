import { ReactElement, useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FormattedMessage, useIntl } from 'react-intl';
import { Joyride, EventData, Step, EVENTS, ACTIONS } from 'react-joyride';
import { Breadcrumb } from  'semantic-ui-react';
import SideBar from './sidebar';
import ProjectSiderbar from './projectSiderbar';
import message from 'components/MessageBox';
import Icon from 'components/Icon';
import TextLimit from 'components/TextLimit';
import { getProjectInfo } from 'services/project';
import { getToggleInfo } from 'services/toggle';
import { getSegmentDetail } from 'services/segment';
import { ISegmentInfo } from 'interfaces/segment';
import { saveDictionary, getFromDictionary } from 'services/dictionary';
import { IDictionary } from 'interfaces/targeting';
import { IProject, IRouterParams } from 'interfaces/project';
import { IToggleInfo } from 'interfaces/targeting';
import { EnvironmentColors } from 'constants/colors';
import { commonConfig, tourStyle } from 'constants/tourConfig';
import { DEMO_TIP_SHOW, USER_GUIDE_LAYOUT } from 'constants/dictionaryKeys';

import {
  TOGGLE_PATH,
  TARGETING_PATH,
  SEGMENT_PATH,
  SEGMENT_ADD_PATH,
  CONNECT_SDK_PATH,
  SEGMENT_EDIT_PATH,
  SETTING_PATH,
  TRACK_EVENT_PATH,
  EVENT_TRACKER_PATH,
} from 'router/routes';
import { useMatchedRoutePath } from 'utils/router';

import styles from './layout.module.scss';
import { useParams } from 'utils/useParams';

interface IProps {
  children: ReactElement
}

const STEPS: Step[] = [
  {
    content: (
      <div className={styles['joyride-content']}>
        <div className='joyride-title'>
          <FormattedMessage id='guide.global.step1.title' />
        </div>
        <ul className='joyride-item'>
          <li><FormattedMessage id='guide.global.step1.project' /></li>
          <li><FormattedMessage id='guide.global.step1.env' /></li>
          <li><FormattedMessage id='guide.global.step1.enter' /></li>
        </ul>
        <div className='joyride-pagination'>1/3</div>
      </div>
    ),
    placement: 'bottom',
    spotlightPadding: 0,
    target: '.joyride-project',
    ...commonConfig
  },
  {
    content: (
      <div>
        <div className='joyride-title'>
          <FormattedMessage id='guide.global.step2.title' />
        </div>
        <ul className='joyride-item'>
          <li><FormattedMessage id='guide.global.step2.env' /></li>
        </ul>
        <div className='joyride-pagination'>2/3</div>
      </div>
    ),
    placement: 'right',
    spotlightPadding: 4,
    target: '.joyride-environment',
    ...commonConfig
  },
  {
    content: (
      <div>
        <div className='joyride-title'>
          <FormattedMessage id='guide.global.step3.title' />
        </div>
        <ul className='joyride-item'>
          <li><FormattedMessage id='segments.card.tips' /></li>
        </ul>
        <div className='joyride-pagination'>3/3</div>
      </div>
    ),
    placement: 'right',
    spotlightPadding: 4,
    target: '.joyride-segment',
    ...commonConfig
  },
];

const ProjectLayout = (props: IProps) => {
  const { projectKey, environmentKey, toggleKey, segmentKey } = useParams<IRouterParams>();
  const [ projectInfo, setProjectInfo ] = useState<IProject>({
    name: '',
    key: '',
    description: '',
    environments: [{
      name: '',
      key: '',
      clientSdkKey: '',
      serverSdkKey: ''
    }]
  });
  const [ envIndex, setEnvIndex ] = useState<number>(0);
  const [ toggleName, saveToggleName ] = useState<string>('');
  const [ segmentName, setSegmentName ] = useState<string>('');
  const [ tipVisible, saveTipVisible ] = useState<boolean>(false);
  const [ isLoading, saveIsLoading ] = useState<boolean>(false);
  const [ run, saveRun ] = useState<boolean>(false);
  const [ stepIndex, saveStepIndex ] = useState<number>(0);
  const navigate = useNavigate();
  const matchedPath = useMatchedRoutePath([
    TARGETING_PATH,
    CONNECT_SDK_PATH,
    TRACK_EVENT_PATH,
    TOGGLE_PATH,
    EVENT_TRACKER_PATH,
    SETTING_PATH,
    SEGMENT_PATH,
    SEGMENT_ADD_PATH,
    SEGMENT_EDIT_PATH,
  ]);
  const intl = useIntl();

  useEffect(() => {
    getProjectInfo<IProject>(projectKey).then(res => {
      saveIsLoading(false);
      if (res.success) {
        const { data } = res;
        if (data) {
          setProjectInfo(data);
        }
      } else {
        message.error(res.message || 'Get project information error!');
      }
    });
  }, [projectKey]);

  useEffect(() => {
    if(segmentKey) {
      getSegmentDetail<ISegmentInfo>(projectKey, segmentKey).then(res => {
        saveIsLoading(false);
        if (res.success) {
          const { data } = res;
          if (data) {
            setSegmentName(data.name);
          }
        } else {
          message.error(res.message || 'Error getting segment');
        }
      });
    }
  }, [projectKey, segmentKey]);

  useEffect(() => {
    if (!toggleKey) return;

    getToggleInfo<IToggleInfo>(projectKey, environmentKey, toggleKey).then(res => {
      const { data, success } = res;

      if (success && data) {
        saveToggleName(data.name);
      }
    });
  }, [projectKey, environmentKey, toggleKey]);

  useEffect(() => {
    const index = projectInfo.environments.findIndex(env => {
      return env.key === environmentKey;
    });
    setEnvIndex(index === -1 ? 0 : index);
  }, [environmentKey, projectInfo.environments]);

  useEffect(() => {
    getFromDictionary<IDictionary>(DEMO_TIP_SHOW).then(res => {
      const { success, data } = res;
      if (success && data) {
        const savedData = JSON.parse(data.value);
        saveTipVisible(savedData);
      } else {
        saveTipVisible(true);
      }
    });

    getFromDictionary<IDictionary>(USER_GUIDE_LAYOUT).then(res => {
      const { success, data } = res;
      if (success && data) {
        const savedData = JSON.parse(data.value);
        if (parseInt(savedData) !== STEPS.length) {
          setTimeout(() => {
            saveRun(true);
          }, 0);
          saveStepIndex(parseInt(savedData));
        }
      } else {
        setTimeout(() => {
          saveRun(true);
        }, 0);
      }
    });
  }, []);

  const gotoProjects = useCallback(() => {
    navigate('/projects');
  }, [navigate]);

  const gotoToggle = useCallback(() => {
    if (!toggleKey) return;
    navigate(`/${projectKey}/${environmentKey}/toggles`);
  }, [navigate, projectKey, environmentKey, toggleKey]);

  const gotoTargeting = useCallback(() => {
    navigate(`/${projectKey}/${environmentKey}/${toggleKey}/targeting`);
  }, [navigate, projectKey, environmentKey, toggleKey]);

  const gotoSegments = useCallback(() => {
    navigate(`/${projectKey}/${environmentKey}/segments`);
  }, [navigate, projectKey, environmentKey]);

  const hideTip = useCallback(() => {
    saveDictionary(DEMO_TIP_SHOW, false).then((res) => {
      if (res.success) {
        saveTipVisible(false);
      }
    });
  }, []);

  const handleJoyrideCallback = useCallback((data: EventData) => {
    const { action, index, type } = data;

    if (([EVENTS.STEP_AFTER, EVENTS.TARGET_NOT_FOUND] as string[]).includes(type)) {
      const nextStepIndex = index + (action === ACTIONS.PREV ? -1 : 1);
      saveStepIndex(nextStepIndex);
      saveDictionary(USER_GUIDE_LAYOUT, nextStepIndex);
    }
  }, []);

  return (
    <div className={styles.main}>
      <Joyride
        run={run}
        onEvent={handleJoyrideCallback}
        stepIndex={stepIndex}
        scrollToFirstStep
options={{
          ...commonConfig,
          buttons: ['back', 'skip', 'primary'] as const,
          showProgress: false,
          dismissKeyAction: false,
          scrollOffset: 100,
        }}
        steps={STEPS}
        locale={{
          'back': intl.formatMessage({id: 'guide.last'}),
          'next': intl.formatMessage({id: 'guide.next'}),
          'last': intl.formatMessage({id: 'guide.done'}),
        }}
        styles={{...tourStyle}}
      />
      <SideBar>
        <ProjectSiderbar
          isLoading={isLoading}
          projectInfo={projectInfo}
          backgroundColor={EnvironmentColors[envIndex % 5]}
        />
      </SideBar>
      <div className={styles['project-content']}>
        {
          tipVisible && (localStorage.getItem('isDemo') === 'true') && (
            <div className={styles['platform-tips']}>
              <Icon type='error-circle' customclass={styles['platform-tips-error']} />
              <span className={styles['platform-tips-text']}>
                <FormattedMessage id='platform.warning.text' />
              </span>
              <Icon type='close' customclass={styles['platform-tips-close']} onClick={() => {hideTip();}} />
            </div>
          )
        }
        <Breadcrumb className={styles.breadcrumb}>
          <Breadcrumb.Section link onClick={gotoProjects}>
            <FormattedMessage id='common.projects.text' />
          </Breadcrumb.Section>
          <Breadcrumb.Divider icon={<Icon customclass={styles['breadcrumb-icon']} type='angle-right' />} />
          {
            matchedPath === TARGETING_PATH && (
              <>
                <Breadcrumb.Section link onClick={gotoToggle}>
                  <TextLimit
                    text={projectInfo.name}
                    maxWidth={190}
                    popupProps={{
                      offset: [0, -12],
                    }}
                  />
                </Breadcrumb.Section>
                <Breadcrumb.Divider icon={<Icon customclass={styles['breadcrumb-icon']} type='angle-right' />} />
                <Breadcrumb.Section active>
                  <TextLimit text={toggleName} maxWidth={190} popupProps={{ offset: [0, -12] }} />
                </Breadcrumb.Section>
              </>
            )
          }
          {
            (matchedPath === CONNECT_SDK_PATH || matchedPath === TRACK_EVENT_PATH) && (
              <>
                <Breadcrumb.Section link onClick={gotoToggle}>
                  <TextLimit text={projectInfo.name} maxWidth={190} popupProps={{ offset: [0, -12] }}  />
                </Breadcrumb.Section>
                <Breadcrumb.Divider icon={<Icon customclass={styles['breadcrumb-icon']} type='angle-right' />} />
                <Breadcrumb.Section link onClick={gotoTargeting}>
                  <TextLimit text={toggleName} maxWidth={190} popupProps={{ offset: [0, -12] }}  />
                </Breadcrumb.Section>
                <Breadcrumb.Divider icon={<Icon customclass={styles['breadcrumb-icon']} type='angle-right' />} />
                <Breadcrumb.Section active>
                  {
                    matchedPath === CONNECT_SDK_PATH
                      ? <FormattedMessage id='common.connect.sdk.text' />
                      : <FormattedMessage id='common.event.track.text' />
                  }
                </Breadcrumb.Section>
              </>
            )
          }
          {
            matchedPath === TOGGLE_PATH && (
              <Breadcrumb.Section active>
                <TextLimit text={projectInfo.name} maxWidth={190} popupProps={{ offset: [0, -12] }}  />
              </Breadcrumb.Section>
            )
          }
          {
            matchedPath === EVENT_TRACKER_PATH && (
              <Breadcrumb.Section active>
                <FormattedMessage id='common.event.tracker.text' />
              </Breadcrumb.Section>
            )
          }
          {
            matchedPath === SETTING_PATH && (
              <Breadcrumb.Section active>
                <FormattedMessage id='common.toggle.settings.text' />
              </Breadcrumb.Section>
            )
          }
          {
            matchedPath === SEGMENT_PATH && (
              <Breadcrumb.Section active><FormattedMessage id='common.segments.text' /></Breadcrumb.Section>
            )
          }
          {
            matchedPath === SEGMENT_ADD_PATH && (
              <>
                <Breadcrumb.Section link onClick={gotoSegments}>
                  <FormattedMessage id='common.segments.text' />
                </Breadcrumb.Section>
                <Breadcrumb.Divider icon={<Icon customclass={styles['breadcrumb-icon']} type='angle-right' />} />
                <Breadcrumb.Section active>
                  <TextLimit text={segmentName} maxWidth={190} popupProps={{ offset: [0, -12] }}  />
                </Breadcrumb.Section>
              </>
            )
          }
          {
            matchedPath === SEGMENT_EDIT_PATH && (
              <>
                <Breadcrumb.Section link onClick={gotoSegments}>
                  <FormattedMessage id='common.segments.text' />
                </Breadcrumb.Section>
                <Breadcrumb.Divider icon={<Icon customclass={styles['breadcrumb-icon']} type='angle-right' />} />
                <Breadcrumb.Section active>
                  <TextLimit text={segmentName} maxWidth={190} popupProps={{ offset: [0, -12] }}  />
                </Breadcrumb.Section>
              </>
            )
          }
        </Breadcrumb>
        {
          (
            matchedPath === SEGMENT_PATH ||
            matchedPath === SEGMENT_ADD_PATH ||
            matchedPath === SEGMENT_EDIT_PATH ||
            matchedPath === SETTING_PATH
          )
            ? null
            : <div style={{background: EnvironmentColors[envIndex % 5]}} className={styles['environment-line']}></div>
        }
        { props.children }
      </div>
    </div>
	);
};

export default ProjectLayout;
