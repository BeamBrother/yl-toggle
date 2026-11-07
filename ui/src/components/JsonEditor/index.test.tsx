import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import { DOMRect } from '../utils/domRect';
import JsonEditor from '.';

Range.prototype.getBoundingClientRect = vi.fn().mockReturnValue(new DOMRect(0, 0, 0, 0));
Range.prototype.getClientRects = vi.fn().mockReturnValue(new Set([new DOMRect(0, 0, 0, 0)]));
Element.prototype.getClientRects = vi.fn().mockReturnValue(new Set([new DOMRect(0, 0, 0, 0)]));

it('JsonEditor snapshot', async () => {
  await (async () => {
    const { asFragment, rerender } = render(<JsonEditor value="error text{{" onChange={vi.fn()} />);
    expect(asFragment()).toMatchSnapshot();

    rerender(<JsonEditor value="{}" onChange={vi.fn()} />);
    expect(asFragment()).toMatchSnapshot();
  })();
});

it('JsonEditor input', async () => {
  await (async () => {
    const mockChange = vi.fn();
    render(<JsonEditor onChange={mockChange} />);
    const textbox = screen.getByRole('textbox');
    await userEvent.type(textbox, 'error text');

    expect(mockChange).toBeCalled();

    await userEvent.clear(textbox);
    await userEvent.type(textbox, '{{}');
    expect(mockChange).toHaveBeenLastCalledWith('{}');
  })();
});
