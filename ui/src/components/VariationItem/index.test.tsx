import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import VariationItem from '.';
import { defaultServeContainer, hooksFormContainer, ruleContainer, variationContainer } from 'pages/toggle/provider';
import { IntlWrapper } from 'components/utils/wrapper';
import { DOMRect } from '../utils/domRect';

vi.setConfig({ testTimeout: 10000 });
Range.prototype.getBoundingClientRect = vi.fn().mockReturnValue(new DOMRect(0, 0, 0, 0));
Range.prototype.getClientRects = vi.fn().mockReturnValue([new DOMRect(0, 0, 0, 0)]);
Element.prototype.getClientRects = vi.fn().mockReturnValue([new DOMRect(0, 0, 0, 0)]);

const Wrapper: React.FC<{ children?: import('react').ReactNode }> = (props) => {
  return (
    <IntlWrapper>
      <hooksFormContainer.Provider>
        <ruleContainer.Provider>
          <defaultServeContainer.Provider>
            <variationContainer.Provider>{props.children}</variationContainer.Provider>
          </defaultServeContainer.Provider>
        </ruleContainer.Provider>
      </hooksFormContainer.Provider>
    </IntlWrapper>
  );
};

it('VariationItem snapshot', async () => {
  await (async () => {
    const { asFragment, rerender } = render(
      <VariationItem
        total={1}
        item={{ index: 1 }}
        returnType="boolean"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        handleDelete={vi.fn()}
        handleChangeVariation={vi.fn()}
        handleInput={vi.fn()}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />,
      {
        wrapper: Wrapper,
      }
    );
    expect(asFragment()).toMatchSnapshot();

    rerender(
      <VariationItem
        total={1}
        item={{ index: 1 }}
        returnType="boolean"
        hooksFormContainer={hooksFormContainer}
        handleDelete={vi.fn()}
        handleChangeVariation={vi.fn()}
        handleInput={vi.fn()}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />
    );
    expect(asFragment()).toMatchSnapshot();
  })();
});

it('VariationItem delete', async () => {
  await (async () => {
    const mockHandleDelete = vi.fn();
    render(
      <VariationItem
        total={1}
        item={{ index: 1 }}
        returnType="boolean"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        handleDelete={mockHandleDelete}
        handleChangeVariation={vi.fn()}
        handleInput={vi.fn()}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />,
      {
        wrapper: Wrapper,
      }
    );

    const ele = screen.getAllByRole('generic').pop();
    if (ele) await userEvent.click(ele);

    expect(mockHandleDelete).toBeCalled();
  })();
});

it('VariationItem input', async () => {
  await (async () => {
    const mockHandleInput = vi.fn();
    const mockHandleChange = vi.fn();
    const { rerender } = render(
      <VariationItem
        total={1}
        item={{ index: 1 }}
        returnType="boolean"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        handleDelete={vi.fn()}
        handleChangeVariation={mockHandleChange}
        handleInput={mockHandleInput}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />,
      {
        wrapper: Wrapper,
      }
    );

    const nameTextbox = screen.getByPlaceholderText('name');
    await userEvent.click(nameTextbox);
    await userEvent.keyboard('t');
    expect(mockHandleInput).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ value: 't' }));
    await userEvent.clear(nameTextbox);

    await userEvent.click(screen.getByPlaceholderText('description'));
    await userEvent.keyboard('d');
    expect(mockHandleInput).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ value: 'd' }));

    await userEvent.click(screen.getByText('Please select'));
    await userEvent.click(screen.getByRole('option', { selected: true }));
    expect(mockHandleChange).toHaveBeenCalledWith(expect.anything(), 'true');

    rerender(
      <VariationItem
        total={1}
        item={{ index: 1 }}
        returnType="string"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        handleDelete={vi.fn()}
        handleChangeVariation={mockHandleChange}
        handleInput={mockHandleInput}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />
    );

    await userEvent.click(screen.getByPlaceholderText('value'));
    await userEvent.keyboard('v');
    expect(mockHandleInput).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ value: 'v' }));
  })();
});

it('VariationItem number input', async () => {
  await (async () => {
    const mockHandleInput = vi.fn();
    const mockHandleChange = vi.fn();
    render(
      <VariationItem
        total={1}
        item={{ index: 1 }}
        returnType="number"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        handleDelete={vi.fn()}
        handleChangeVariation={mockHandleChange}
        handleInput={mockHandleInput}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />,
      {
        wrapper: Wrapper,
      }
    );

    await userEvent.click(screen.getByPlaceholderText('value'));
    await userEvent.keyboard('v');
    await userEvent.keyboard('{backspace}');
    await userEvent.keyboard('100');
    expect(mockHandleInput).toHaveBeenLastCalledWith(expect.anything(), expect.objectContaining({ value: '100' }));
  })();
});

it('VariationItem json modal', async () => {
  await (async () => {
    const mockHandleInput = vi.fn();
    const mockHandleChange = vi.fn();
    render(
      <VariationItem
        total={1}
        item={{ index: 1 }}
        returnType="json"
        prefix="drawer"
        hooksFormContainer={hooksFormContainer}
        handleDelete={vi.fn()}
        handleChangeVariation={mockHandleChange}
        handleInput={mockHandleInput}
        ruleContainer={ruleContainer}
        defaultServeContainer={defaultServeContainer}
      />,
      {
        wrapper: Wrapper,
      }
    );
    await userEvent.click(screen.getByPlaceholderText('value'));
    await userEvent.keyboard('{{}');
    expect(mockHandleInput).toHaveBeenLastCalledWith(expect.anything(), expect.objectContaining({ value: '{}' }));

    const jsonModalOpen = document.getElementsByClassName('icon-code')[0];
    expect(jsonModalOpen).not.toBeUndefined();
    await userEvent.click(jsonModalOpen);

    await userEvent.click(screen.getByText('Cancel'));

    await userEvent.click(jsonModalOpen);

    const textbox = screen.getAllByRole('textbox')[3];
    await userEvent.type(textbox, 'error text{{{{');
    expect(screen.getByText('Confirm')).toBeDisabled();

    await userEvent.clear(textbox);
    await userEvent.type(textbox, '{{}');

    await userEvent.click(screen.getByText('Confirm'));
    expect(mockHandleChange).toHaveBeenLastCalledWith(1, '{}');
  })();
});
