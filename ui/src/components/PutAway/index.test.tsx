import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import { SidebarContainer } from 'layout/hooks';
import { useEffect } from 'react';
import { IntlWrapper } from 'components/utils/wrapper';
import PutAway from '.';

const TrueWrapper: React.FC<{ children?: import('react').ReactNode }> = (props) => {
  return (
    <IntlWrapper>
      <SidebarContainer.Provider>
        <UseHookWrapper isPutAway={true}>
          {props.children}
          <div id="footer"></div>
        </UseHookWrapper>
      </SidebarContainer.Provider>
    </IntlWrapper>
  );
};

const FalseWrapper: React.FC<{ children?: import('react').ReactNode }> = (props) => {
  return (
    <IntlWrapper>
      <SidebarContainer.Provider>
        <UseHookWrapper isPutAway={false}>{props.children}</UseHookWrapper>
      </SidebarContainer.Provider>
    </IntlWrapper>
  );
};

const UseHookWrapper: React.FC<{ children?: import('react').ReactNode; isPutAway?: boolean }> = ({ children, isPutAway }) => {
  const { setIsputAway } = SidebarContainer.useContainer();

  useEffect(() => {
    setIsputAway(isPutAway);
  }, [isPutAway]);

  return <>{children}</>;
};

it('PutAway true snapshot', async () => {
  await (async () => {
    const { baseElement } = render(<PutAway />, {
      wrapper: TrueWrapper,
    });
    expect(baseElement).toMatchSnapshot();
  })();
});

it('PutAway false snapshot', async () => {
  await (async () => {
    const { baseElement } = render(<PutAway />, {
      wrapper: FalseWrapper,
    });
    expect(baseElement).toMatchSnapshot();
  })();
});

it('click PutAway', async () => {
  await (async () => {
    const { asFragment } = render(<PutAway />, {
      wrapper: FalseWrapper,
    });

    await userEvent.click(screen.getByText('Collapse'));
    expect(asFragment()).toMatchSnapshot();
  })();
});
