import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import Variations from '.';
import { hooksFormContainer, variationContainer } from 'pages/toggle/provider';
import { IntlWrapper } from 'components/utils/wrapper';
import { useEffect } from 'react';
import { defaultServeContainer, ruleContainer } from 'pages/targeting/provider';

const Wrapper: React.FC<{ children?: import('react').ReactNode }> = (props) => {
  return (
    <IntlWrapper>
      <hooksFormContainer.Provider>
        <variationContainer.Provider>
          <ruleContainer.Provider>
            <defaultServeContainer.Provider>
              <UseHookWrapper
                data={[
                  {
                    id: 'test_id',
                    value: '',
                    name: 'name',
                    description: '',
                  },
                ]}
              >
                {props.children}
              </UseHookWrapper>
            </defaultServeContainer.Provider>
          </ruleContainer.Provider>
        </variationContainer.Provider>
      </hooksFormContainer.Provider>
    </IntlWrapper>
  );
};

const ErrorWrapper: React.FC<{ children?: import('react').ReactNode }> = (props) => {
  return (
    <IntlWrapper>
      <hooksFormContainer.Provider>
        <variationContainer.Provider>
          <ruleContainer.Provider>
            <defaultServeContainer.Provider>
              <UseHookWrapper
                data={[
                  {
                    id: 'test_id',
                    value: '',
                    name: 'name',
                    description: '',
                  },
                  {
                    id: 'test_id2',
                    value: '',
                    name: 'name',
                    description: '',
                  },
                ]}
              >
                {props.children}
              </UseHookWrapper>
            </defaultServeContainer.Provider>
          </ruleContainer.Provider>
        </variationContainer.Provider>
      </hooksFormContainer.Provider>
    </IntlWrapper>
  );
};

const UseHookWrapper: React.FC<{ children?: import('react').ReactNode; data: unknown }> = ({ children, data }) => {
  const { saveVariations } = variationContainer.useContainer();

  useEffect(() => {
    saveVariations(data);
  }, [saveVariations, data]);

  return <>{children}</>;
};

it('Variations snapshot', async () => {
  await (async () => {
    const { asFragment, rerender } = render(
      <Variations
        returnType="boolean"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        variationContainer={variationContainer}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />,
      {
        wrapper: Wrapper,
      }
    );
    expect(asFragment()).toMatchSnapshot();

    rerender(
      <Variations
        returnType="number"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        variationContainer={variationContainer}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />
    );
    expect(asFragment()).toMatchSnapshot();

    rerender(
      <Variations
        returnType="string"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        variationContainer={variationContainer}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />
    );
    expect(asFragment()).toMatchSnapshot();

    rerender(
      <Variations
        returnType="json"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        variationContainer={variationContainer}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />
    );
    expect(asFragment()).toMatchSnapshot();
  })();
});

it('Variations error name', async () => {
  await (async () => {
    const { asFragment } = render(
      <Variations
        returnType="boolean"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        variationContainer={variationContainer}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />,
      {
        wrapper: ErrorWrapper,
      }
    );

    expect(asFragment()).toMatchSnapshot();
  })();
});

it('Add Variations and delete', async () => {
  await (async () => {
    const { baseElement } = render(
      <Variations
        returnType="boolean"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        variationContainer={variationContainer}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />,
      {
        wrapper: Wrapper,
      }
    );

    await userEvent.click(screen.getByText('Add Variation'));
    await userEvent.click(screen.getByText('Add Variation'));
    await userEvent.click(screen.getByText('Add Variation'));

    const matchDiv = document.createElement('div');
    matchDiv.innerHTML = baseElement.innerHTML?.replaceAll(/variation_[0-9a-fA-F-]{36}/g, 'variation_test');

    expect(matchDiv).toMatchSnapshot();

    const dels = document.querySelectorAll('span.icon-minus');
    await userEvent.click(dels[0]);
    matchDiv.innerHTML = baseElement.innerHTML?.replaceAll(/variation_[0-9a-fA-F-]{36}/g, 'variation_test');
    expect(matchDiv).toMatchSnapshot();
  })();
});

it('Add Variations and input', async () => {
  await (async () => {
    render(
      <Variations
        returnType="boolean"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        variationContainer={variationContainer}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />,
      {
        wrapper: Wrapper,
      }
    );

    await userEvent.click(screen.getByText('Add Variation'));
    await userEvent.click(screen.getByText('Add Variation'));
    await userEvent.click(screen.getByText('Add Variation'));

    await userEvent.type(screen.getAllByPlaceholderText('name')[0], 'name');
    await userEvent.type(screen.getAllByPlaceholderText('name')[1], 'name');
  })();
});
