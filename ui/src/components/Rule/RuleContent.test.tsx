import React, { ReactElement, useEffect } from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import { DragDropContext, Droppable } from 'react-beautiful-dnd';
import userEvent from '@testing-library/user-event';
import RuleContent from '.';
import { IntlWrapper } from 'components/utils/wrapper';
import { hooksFormContainer, ruleContainer, segmentContainer, variationContainer } from 'pages/targeting/provider';
import { act } from 'react';

const RuleData = {
  id: 'testId1',
  name: '',
  serve: undefined,
  conditions: [],
  active: true,
};

const Wrapper: React.FC<{ children?: import('react').ReactNode }> = ({ children }) => {
  return (
    <IntlWrapper>
      <hooksFormContainer.Provider>
        <ruleContainer.Provider>
          <segmentContainer.Provider>
            <variationContainer.Provider>
              <DragDropContext onDragEnd={vi.fn()}>
                <Droppable droppableId="droppable">
                  {(provided) => (
                    <div {...provided.droppableProps} ref={provided.innerRef}>
                      <div>
                        <UseHookWrapper>{children as ReactElement<Record<string, unknown>>}</UseHookWrapper>
                      </div>
                      Í{provided.placeholder}
                    </div>
                  )}
                </Droppable>
              </DragDropContext>
            </variationContainer.Provider>
          </segmentContainer.Provider>
        </ruleContainer.Provider>
      </hooksFormContainer.Provider>
    </IntlWrapper>
  );
};

const UseHookWrapper: React.FC<{ children: ReactElement<Record<string, unknown>> }> = ({ children }) => {
  const { saveRules } = ruleContainer.useContainer();
  const { saveVariations } = variationContainer.useContainer();
  const useSegment = segmentContainer.useContainer();
  const onlychildren = React.Children.only(children);

  const reChildren = React.cloneElement(
    onlychildren,
    {
      ...children.props,
      useSegment: useSegment,
    },
    children
  );

  useEffect(() => {
    saveRules([RuleData]);
    saveVariations([
      {
        id: 'test_id',
        value: '',
        name: 'name',
        description: '',
      },
    ]);
  }, [saveRules, saveVariations]);

  return <>{reChildren}</>;
};

it('RuleContent snapshot', async () => {
  const { asFragment, rerender } = render(
    <RuleContent
      rule={RuleData}
      index={0}
      ruleContainer={ruleContainer}
      segmentContainer={segmentContainer}
      hooksFormContainer={hooksFormContainer}
      subjectOptions={[]}
    />,
    {
      wrapper: Wrapper,
    }
  );

  expect(asFragment()).toMatchSnapshot();

  rerender(
    <RuleContent
      rule={RuleData}
      index={0}
      ruleContainer={ruleContainer}
      segmentContainer={segmentContainer}
      hooksFormContainer={hooksFormContainer}
      subjectOptions={[]}
      disabled={true}
      active={true}
      useSegment
    />
  );

  expect(asFragment()).toMatchSnapshot();
});

it('RuleContent opt', async () => {
  await (async () => {
    const { baseElement } = render(
      <RuleContent
        rule={RuleData}
        index={0}
        ruleContainer={ruleContainer}
        segmentContainer={segmentContainer}
        hooksFormContainer={hooksFormContainer}
        subjectOptions={[]}
        active={true}
        variationContainer={variationContainer}
      />,
      {
        wrapper: Wrapper,
      }
    );

    await act(async () => {
      await userEvent.click(screen.getByText('Add'));
      expect(baseElement).toMatchSnapshot();
      await userEvent.click(screen.getByText('number'));
      await userEvent.click(screen.getByText('semver'));
      await userEvent.click(screen.getByText('segment'));

      const serve = screen.getAllByText('Please select').pop();
      serve && (await userEvent.click(serve));
      userEvent.click(screen.getByText('a percentage rollout'));
    });

    const ele = document.createElement('div');
    ele.innerHTML = baseElement.innerHTML
      .replaceAll(/(\d\d\/\d\d\/\d\d\d\d )*\d\d:\d\d:\d\d/g, 'test_value')
      .replaceAll(/\sname="rule_testId1_[\w-]+\w" /g, (_, i) => {
        return ` name="test-name-${i}"`;
      })
      .replaceAll(' rdtActive rdtToday', '');
    expect(ele).toMatchSnapshot();
  })();
});
